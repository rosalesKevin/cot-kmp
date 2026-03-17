# Detail Preservation Notes

This file explains, in simple terms, what happens to `<detail>` content when this library
parses and serializes a CoT message.

## In short

- Common fields are easy to access
- Custom detail tags are kept
- Nested detail content is kept
- XML special characters are handled for you

That means the library tries to be convenient for common cases without throwing away the
extra detail data that many CoT messages carry.

## Preserved Content

- `<contact callsign="...">` becomes `CotDetail.callsign`
- `<contact phone="...">` becomes `CotDetail.phone`
- `<remarks>...</remarks>` becomes `CotDetail.remarks`
- Any other child under `<detail>` is kept as a `DetailElement`
- Nested detail elements are kept recursively
- Attributes and text are unescaped when reading and escaped again when writing

## Parsing Notes

- The input may include or omit an XML declaration
- A self-closing `<detail/>` becomes an empty `CotDetail`, not `null`
- Unknown child elements directly under `<event>` are skipped if they are not `<point>` or `<detail>`

## Intended Round-Trip Behavior

- `callsign` and `phone` should survive parse -> serialize -> parse
- `remarks` text should survive parse -> serialize -> parse, including XML special characters
- Other detail children should survive parse -> serialize -> parse as structured `DetailElement` trees

## Source of Truth

The expected behavior here should match the package tests in:

- `lib/src/commonTest/kotlin/cot/CotParserTest.kt`
- `lib/src/commonTest/kotlin/cot/CotSerializerTest.kt`
