# KotCOT

KotCOT is a lightweight Kotlin Multiplatform library for parsing, generating, and transforming Cursor on Target (COT) messages. It targets Android, JVM/Desktop, and JavaScript.

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

### Convert SIDC back to COT type

```kotlin
import cot.sidc.sidcToCotType

val cotType = sidcToCotType("SFGPUC---------")
```

## Detail Extensibility

The `CotDetail` model exposes first-class fields for the two most common children (`<contact>` and `<remarks>`). All other child elements — including ATAK-specific extensions like `<__group>`, `<takv>`, and `<status>` — are captured as a `List<DetailElement>` in `CotDetail.children`.

Each `DetailElement` carries the tag name, a map of attributes, optional text content, and a list of nested children. Values are already XML-unescaped.

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
            tag        = "status",
            attributes = mapOf("battery" to "87"),
        ),
    ),
)
```

## Interoperability Contract

### XML parsing

- Input may include or omit an XML declaration (`<?xml version="1.0"?>`).
- XML entity references (`&amp;`, `&lt;`, `&gt;`, `&quot;`, `&apos;`) are unescaped
  automatically; special characters in serialized output are always properly escaped.
- A self-closing `<detail/>` is parsed as an empty `CotDetail` (no children, all
  fields null) rather than `null`.
- Unknown `<event>` child elements (anything other than `<point>` and `<detail>`) are
  skipped without error.

### SIDC round-trip guarantees

| Standard        | Guaranteed lossless                                               | Known lossy cases |
|-----------------|-------------------------------------------------------------------|-------------------|
| MIL-STD-2525B   | All 8 affiliations, all 7 dimensions, function + modifier codes   | None |
| MIL-STD-2525C   | All 8 affiliations, all 7 dimensions, function + modifier codes   | None |
| MIL-STD-2525D   | All affiliations on GROUND/SPACE/SEA/SUBSURFACE/SOF; hostile-air  | Non-hostile AIR encodes to the same symbol set as GROUND — decodes to GROUND. Friendly/neutral/unknown AIR is lost. JOKER and FAKER share affiliation code `5` with SUSPECT — both decode as SUSPECT. |

**AIR vs GROUND ambiguity in 2525D:** Symbol set `10` is shared by AIR, GROUND, and
OTHER. The decoder resolves this with an affiliation heuristic: HOSTILE + symbol set 10
→ AIR; all other affiliations → GROUND. As a result, hostile-air is lossless but
friendly/neutral/unknown air degrades to GROUND after a 2525D encode→decode cycle.

**JOKER / FAKER in 2525D:** Both are encoded as affiliation code `5` (same as SUSPECT).
They cannot be recovered from a 2525D SIDC alone; `sidcToCotType` returns `a-s-*`
(SUSPECT) in all three cases.

### `detail` preservation

- `callsign` and `phone` (from `<contact>`) round-trip exactly.
- `remarks` text content round-trips exactly, including special characters.
- All other `<detail>` children are preserved as structured `DetailElement` values and
  round-trip exactly through parse → serialize → re-parse.

## Error Handling

Parsing and transformation APIs return `Result<T>` for normal failures. Invalid XML, malformed COT type strings, and unsupported SIDC values produce descriptive error messages.

## COT Type Format Reference

`scheme-affiliation-dimension[-function[-modifier]]`

Examples:

- `a-f-G-U-C`
- `a-h-A`
- `a-n-S`

## Installation

### Via Maven Central *(stable releases — recommended)*

No extra repository configuration needed.

**Gradle (Kotlin DSL):**
```kotlin
dependencies {
    implementation("io.github.rosaleskevin:kotcot:0.1.0-alpha01")
}
```

**Gradle (Groovy):**
```groovy
dependencies {
    implementation 'io.github.rosaleskevin:kotcot:0.1.0-alpha01'
}
```

**Maven:**
```xml
<dependency>
    <groupId>io.github.rosaleskevin</groupId>
    <artifactId>kotcot</artifactId>
    <version>0.1.0-alpha01</version>
</dependency>
```

---

### Via JitPack *(alpha / pre-release versions)*

**Step 1** — Add JitPack to your repositories:

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

**Step 2** — Add the dependency:

```kotlin
dependencies {
    implementation("com.github.rosalesKevin:kotcot:0.1.0-alpha01")
}
```

---

### Via npm *(JavaScript / TypeScript)*

```bash
npm install kotcot
```

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.
You are free to use, modify, and distribute this library in both open source and
commercial/proprietary projects with no restrictions beyond attribution.
