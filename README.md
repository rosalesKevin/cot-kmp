# cot-kmp

cot-kmp is a lightweight Kotlin Multiplatform library for parsing, generating, and transforming Cursor on Target (COT) messages. It targets Android, JVM/Desktop, and JavaScript.

## Targets

- Android API 21+
- JVM 17+
- JavaScript Browser (IR)

## Quick Start

### Parse a COT message

```kotlin
import cot.parser.CotParser

val result = CotParser.parse(
    """
    <event version="2.0" uid="UNIT-001" type="a-f-G-U-C"
      time="2024-01-01T00:00:00.000Z" start="2024-01-01T00:00:00.000Z"
      stale="2024-01-01T01:00:00.000Z" how="m-g">
      <point lat="34.0522" lon="-118.2437" hae="71.0" ce="10.0" le="5.0"/>
    </event>
    """.trimIndent(),
)
```

### Build and serialize a COT message

```kotlin
import cot.model.CotEvent
import cot.model.CotPoint
import cot.serializer.CotSerializer

val event = CotEvent(
    uid = "UNIT-001",
    type = "a-f-G-U-C",
    time = "2024-01-01T00:00:00.000Z",
    start = "2024-01-01T00:00:00.000Z",
    stale = "2024-01-01T01:00:00.000Z",
    how = "m-g",
    point = CotPoint(lat = 34.0522, lon = -118.2437),
)

val xml = CotSerializer.serialize(event)
```

### Convert COT type to SIDC

```kotlin
import cot.sidc.SidcStandard
import cot.sidc.cotTypeToSidc

val sidc2525C = cotTypeToSidc("a-f-G-U-C", SidcStandard.MIL_STD_2525C)
val sidc2525D = cotTypeToSidc("a-f-G-U-C", SidcStandard.MIL_STD_2525D)
```

Supported SIDC standards in this library:

- MIL-STD-2525B
- MIL-STD-2525C
- MIL-STD-2525D

### Convert SIDC back to COT type

```kotlin
import cot.sidc.sidcToCotType

val cotType = sidcToCotType("SFGPUC---------")
```

## Detail Extensibility

`CotDetail` gives you simple access to common detail values like contact info and remarks, while still keeping custom detail elements available when you need them.

If your messages include ATAK or vendor-specific detail tags, they are preserved in a structured form instead of being discarded.

```kotlin
val event = CotParser.parse(xml).getOrThrow()
val group = event.detail?.children?.firstOrNull { it.tag == "__group" }
val role  = group?.attributes?.get("role") // e.g. "Team Member"
```

To add a custom child when building a message:

```kotlin
import cot.model.DetailElement

val detail = CotDetail(
    callsign = "ALPHA-1",
    children = listOf(
        DetailElement(
            tag = "status",
            attributes = mapOf("battery" to "87"),
        ),
    ),
)
```

For more detail about what is preserved and how it round-trips, see [DETAIL_PRESERVATION.md](src/commonMain/kotlin/cot/model/DETAIL_PRESERVATION.md).

## Error Handling

Parsing and transformation APIs return `Result<T>` for normal failures. Invalid XML, malformed COT type strings, and unsupported SIDC values produce descriptive error messages.

## COT Type Format Reference

`scheme-affiliation-dimension[-function[-modifier]]`

Examples:

- `a-f-G-U-C`
- `a-h-A`
- `a-n-S`

For more detail about SIDC support, legacy 2525B interoperability, and known ambiguities, see [SIDC_INTEROP.md](src/commonMain/kotlin/cot/sidc/SIDC_INTEROP.md).

## Installation

### Via Maven Central *(stable releases - recommended)*

No extra repository configuration needed.

**Gradle (Kotlin DSL):**
```kotlin
dependencies {
    implementation("io.github.rosaleskevin:cot-kmp:0.1.0-alpha01")
}
```

**Gradle (Groovy):**
```groovy
dependencies {
    implementation 'io.github.rosaleskevin:cot-kmp:0.1.0-alpha01'
}
```

**Maven:**
```xml
<dependency>
    <groupId>io.github.rosaleskevin</groupId>
    <artifactId>cot-kmp</artifactId>
    <version>0.1.0-alpha01</version>
</dependency>
```

---

### Via JitPack *(alpha / pre-release versions)*

**Step 1** - Add JitPack to your repositories:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**Step 2** - Add the dependency:

```kotlin
dependencies {
    implementation("com.github.rosalesKevin:cot-kmp:0.1.0-alpha01")
}
```

---

### Via npm *(JavaScript / TypeScript)*

```bash
npm install cot-kmp
```

---

## License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.
You are free to use, modify, and distribute this library in both open source and
commercial/proprietary projects with no restrictions beyond attribution.
