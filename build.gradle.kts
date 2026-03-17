import com.vanniktech.maven.publish.SonatypeHost
import java.util.Properties
import java.nio.file.Files
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.process.ExecOperations
import javax.inject.Inject

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }

    jvm()

    js(IR) {
        // browser() is kept for distribution: it configures the compiled output format
        // and package.json metadata for npm consumers.
        // Browser TESTS are disabled: the Karma test runner requires fetching
        // "github:Kotlin/karma" and fails on Windows (UNKNOWN lstat error in npm/yarn).
        // Tests run instead via jsNodeTest (see nodejs() below).
        browser {
            testTask { enabled = false }
        }
        nodejs()  // runs tests in Node.js via Mocha — no Karma, no browser required
        // npm package metadata — consumed by JS/TS projects installing cot-kmp via npm.
        compilations["main"].packageJson {
            name        = "cot-kmp"
            version     = "0.1.0-alpha04"
            description = "Lightweight Kotlin Multiplatform library for COT parsing and SIDC conversion."
            customField("homepage", "https://github.com/rosalesKevin/cot-kmp")
            customField("author",  "Kevin Klein Rosales")
            customField("license", "MIT")
            customField("repository", mapOf(
                "type" to "git",
                "url"  to "git+https://github.com/rosalesKevin/cot-kmp.git",
            ))
            customField("bugs", mapOf(
                "url" to "https://github.com/rosalesKevin/cot-kmp/issues",
            ))
        }
        generateTypeScriptDefinitions()
        binaries.library()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.xmlutil.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace  = "io.github.rosaleskevin.cotkmp"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

// ── Publish metadata ──────────────────────────────────────────────────────────
val publishProps = Properties().apply {
    load(rootProject.file("gradle/publish.properties").inputStream())
}
fun prop(key: String): String = publishProps.getProperty(key)
    ?: error("Missing publish property: $key")

group   = prop("GROUP_ID")
version = prop("VERSION")

// ── Maven Central (vanniktech) ────────────────────────────────────────────────
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    // Sign only when signing key is available (CI sets this env var; local builds skip signing).
    if (System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey") != null) {
        signAllPublications()
    }
    coordinates(prop("GROUP_ID"), prop("ARTIFACT_ID"), prop("VERSION"))

    pom {
        name.set(prop("POM_NAME"))
        description.set(prop("POM_DESCRIPTION"))
        inceptionYear.set("2025")
        url.set(prop("POM_URL"))

        licenses {
            license {
                name.set(prop("POM_LICENSE_NAME"))
                url.set(prop("POM_LICENSE_URL"))
                distribution.set(prop("POM_LICENSE_DIST"))
            }
        }

        developers {
            developer {
                id.set(prop("POM_DEVELOPER_ID"))
                name.set(prop("POM_DEVELOPER_NAME"))
                url.set(prop("POM_DEVELOPER_URL"))
            }
        }

        scm {
            url.set(prop("POM_SCM_URL"))
            connection.set(prop("POM_SCM_CONN"))
            developerConnection.set(prop("POM_SCM_DEV_CONN"))
        }
    }
}

// ── JS consumer smoke test ────────────────────────────────────────────────────
// Run: ./gradlew verifyJsConsumer --no-daemon
// Builds the npm package, copies it into test-javascript/node_modules/cot-kmp,
// and runs "npm test" to verify the published JS surface is intact.
@UntrackedTask(because = "Executes external npm process; always run when invoked")
abstract class VerifyJsConsumerTask @Inject constructor(
    private val execOps: ExecOperations,
    objects: ObjectFactory,
) : DefaultTask() {

    @get:InputDirectory
    val srcDir: DirectoryProperty = objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("dist/js/productionLibrary"))

    @TaskAction
    fun run() {
        val src  = srcDir.get().asFile
        val dest = project.file("../test-javascript/node_modules/cot-kmp")
        val destPath = dest.toPath()
        // Cleanup strategy:
        // 1. Try Files.delete() first — removes junctions/reparse-points atomically on Windows.
        // 2. If it fails with DirectoryNotEmptyException it's a real directory; fall back to deleteRecursively().
        // deleteRecursively() alone would throw "untrusted mount point" when traversing junctions.
        if (Files.exists(destPath) || Files.isSymbolicLink(destPath)) {
            try {
                Files.delete(destPath)
            } catch (_: java.nio.file.DirectoryNotEmptyException) {
                dest.deleteRecursively()
            }
        }
        src.copyRecursively(dest, overwrite = true)

        val npmCmd = if (System.getProperty("os.name").lowercase().contains("windows")) "npm.cmd" else "npm"
        execOps.exec {
            workingDir(project.file("../test-javascript"))
            commandLine(npmCmd, "test")
        }
    }
}

tasks.register<VerifyJsConsumerTask>("verifyJsConsumer") {
    dependsOn("jsNodeProductionLibraryDistribution")
}

// ── npm README ────────────────────────────────────────────────────────────────
// Copies README.npm.md into the JS distribution as README.md so that
// npm shows documentation on the package page after `npm publish`.
tasks.named("jsNodeProductionLibraryDistribution") {
    doLast {
        val src  = project.file("README.npm.md")
        val dest = layout.buildDirectory.file("dist/js/productionLibrary/README.md").get().asFile
        src.copyTo(dest, overwrite = true)
    }
}
