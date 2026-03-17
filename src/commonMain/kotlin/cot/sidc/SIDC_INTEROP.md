# SIDC Interoperability Notes

This file explains how the SIDC conversion behaves in practice, especially where the
different military standards do not map perfectly to each other.

## In short

- 2525B and 2525C round-trip cleanly in this library
- 2525D has a few unavoidable ambiguities
- 2525B matters for legacy interoperability and stays supported

## Round-Trip Guarantees

| Standard        | Guaranteed lossless                                               | Known lossy cases |
|-----------------|-------------------------------------------------------------------|-------------------|
| MIL-STD-2525B   | All 8 affiliations, all 7 dimensions, function + modifier codes   | None |
| MIL-STD-2525C   | All 8 affiliations, all 7 dimensions, function + modifier codes   | None |
| MIL-STD-2525D   | All affiliations on GROUND/SPACE/SEA/SUBSURFACE/SOF; hostile-air  | Non-hostile AIR encodes to the same symbol set as GROUND and decodes to GROUND. JOKER and FAKER share affiliation code `5` with SUSPECT and decode as SUSPECT. |

## 2525B vs 2525C

- 2525B and 2525C are both 15-character SIDC forms.
- The main difference here is AIR handling.
- In 2525B, AIR with no function or modifier uses `------`.
- In 2525C, AIR with no function or modifier uses `AA----`.
- If legacy interoperability matters, callers should pass the standard explicitly instead of relying on 15-character auto-detection.

## 2525D Ambiguities

- Symbol set `10` is shared by AIR, GROUND, and OTHER.
- The decoder uses a simple rule here: hostile plus symbol set `10` maps to AIR, while other affiliations map to GROUND.
- Because of that, friendly, neutral, assumed-friendly, and unknown AIR values are lossy through encode -> decode.
- JOKER and FAKER also cannot be recovered from a bare 2525D SIDC because they share affiliation code `5` with SUSPECT.

## Source of Truth

The expected behavior here should match the package tests in:

- `lib/src/commonTest/kotlin/cot/SidcTransformTest.kt`
