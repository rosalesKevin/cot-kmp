import com.vanniktech.maven.publish.SonatypeHost
import java.util.Properties

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
            version     = "0.1.0-alpha01"
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
