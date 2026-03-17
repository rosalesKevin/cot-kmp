# cot-kmp

Lightweight library for parsing, building, and converting [Cursor on Target (COT)](https://www.mitre.org/sites/default/files/pdf/09_4937.pdf) messages, with full MIL-STD-2525B/C/D SIDC symbology support.

Published as a Kotlin Multiplatform library — available for Android, JVM, and **JavaScript/TypeScript** (this package).

## Installation

```bash
npm install cot-kmp
```

## Quick Start

```javascript
import * as cot from "cot-kmp";

// Resolve the API namespace — handles both ESM and CommonJS output shapes
const root = cot.default ?? cot;
const api = root.cot ?? root;

const xml = `<event version="2.0" uid="UNIT-001" type="a-f-G-U-C"
  time="2024-01-01T00:00:00.000Z" start="2024-01-01T00:00:00.000Z"
  stale="2024-01-01T01:00:00.000Z" how="m-g">
  <point lat="34.0522" lon="-118.2437" hae="71.0" ce="10.0" le="5.0"/>
</event>`;

// Parse
const parseResult = api.CotJsApi.parse(xml);
if (!parseResult.ok) throw new Error(parseResult.error);
const event = parseResult.value;

console.log(event.uid);   // UNIT-001
console.log(event.type);  // a-f-G-U-C

// Serialize
const serializedResult = api.CotJsApi.serialize(event);
if (!serializedResult.ok) throw new Error(serializedResult.error);
console.log(serializedResult.value); // <event .../>

// COT type → SIDC
const sidcResult = api.CotJsApi.cotTypeToSidc(event.type, api.JsSidcStandard.MIL_STD_2525C);
if (!sidcResult.ok) throw new Error(sidcResult.error);
console.log(sidcResult.value); // SFGPUC---------

// SIDC → COT type
const reverseResult = api.CotJsApi.sidcToCotType(sidcResult.value, api.JsSidcStandard.MIL_STD_2525C);
if (!reverseResult.ok) throw new Error(reverseResult.error);
console.log(reverseResult.value); // a-f-G-U-C
```

## API

All methods return a result envelope `{ ok: true, value: T }` or `{ ok: false, error: string }` — no exceptions thrown.

| Method | Parameters | Returns |
|--------|-----------|---------|
| `CotJsApi.parse(xml)` | `xml: string` | `JsCotEventResult` |
| `CotJsApi.serialize(event)` | `event: CotEvent` | `JsStringResult` |
| `CotJsApi.cotTypeToSidc(cotType, standard)` | `cotType: string`, `standard: JsSidcStandard` | `JsStringResult` |
| `CotJsApi.sidcToCotType(sidc, standard)` | `sidc: string`, `standard: JsSidcStandard` | `JsStringResult` |

### SIDC Standards

```javascript
api.JsSidcStandard.MIL_STD_2525B
api.JsSidcStandard.MIL_STD_2525C
api.JsSidcStandard.MIL_STD_2525D
```

## TypeScript

Type definitions (`.d.ts`) are included in the package.

## COT Type Format

`scheme-affiliation-dimension[-function[-modifier]]`

Examples: `a-f-G-U-C` (friendly ground unit), `a-h-A` (hostile air), `a-n-S` (neutral surface)

## Links

- [GitHub](https://github.com/rosalesKevin/cot-kmp)
- [Maven Central (JVM/Android)](https://central.sonatype.com/artifact/io.github.rosaleskevin/cot-kmp)
- [Issues](https://github.com/rosalesKevin/cot-kmp/issues)

## License

MIT
