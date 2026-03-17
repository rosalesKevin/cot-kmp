package cot

import cot.sidc.SidcStandard
import cot.sidc.cotTypeToSidc
import cot.sidc.sidcToCotType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SidcTransformTest {

    @Test
    fun forwardConversionAllAffiliations2525C() {
        // Scheme a, dimension G (ground), all affiliations
        assertEquals("SUGP-----------", cotTypeToSidc("a-u-G", SidcStandard.MIL_STD_2525C).getOrThrow()) // UNKNOWN
        assertEquals("SFGP-----------", cotTypeToSidc("a-f-G", SidcStandard.MIL_STD_2525C).getOrThrow()) // FRIENDLY
        assertEquals("SHGP-----------", cotTypeToSidc("a-h-G", SidcStandard.MIL_STD_2525C).getOrThrow()) // HOSTILE
        assertEquals("SNGP-----------", cotTypeToSidc("a-n-G", SidcStandard.MIL_STD_2525C).getOrThrow()) // NEUTRAL
        assertEquals("SAGP-----------", cotTypeToSidc("a-a-G", SidcStandard.MIL_STD_2525C).getOrThrow()) // ASSUMED_FRIENDLY
        assertEquals("SSGP-----------", cotTypeToSidc("a-s-G", SidcStandard.MIL_STD_2525C).getOrThrow()) // SUSPECT
        assertEquals("SJGP-----------", cotTypeToSidc("a-j-G", SidcStandard.MIL_STD_2525C).getOrThrow()) // JOKER
        assertEquals("SKGP-----------", cotTypeToSidc("a-k-G", SidcStandard.MIL_STD_2525C).getOrThrow()) // FAKER
    }

    @Test
    fun forwardConversionAllDimensions2525C() {
        // Affiliation f (friendly), all 7 dimensions
        assertEquals("SFPP-----------", cotTypeToSidc("a-f-P", SidcStandard.MIL_STD_2525C).getOrThrow()) // SPACE
        assertEquals("SFAPAA---------", cotTypeToSidc("a-f-A", SidcStandard.MIL_STD_2525C).getOrThrow()) // AIR
        assertEquals("SFGP-----------", cotTypeToSidc("a-f-G", SidcStandard.MIL_STD_2525C).getOrThrow()) // GROUND
        assertEquals("SFSP-----------", cotTypeToSidc("a-f-S", SidcStandard.MIL_STD_2525C).getOrThrow()) // SEA_SURFACE
        assertEquals("SFUP-----------", cotTypeToSidc("a-f-U", SidcStandard.MIL_STD_2525C).getOrThrow()) // SUBSURFACE
        assertEquals("SFFP-----------", cotTypeToSidc("a-f-F", SidcStandard.MIL_STD_2525C).getOrThrow()) // SOF
        assertEquals("SFXP-----------", cotTypeToSidc("a-f-X", SidcStandard.MIL_STD_2525C).getOrThrow()) // OTHER
    }

    @Test
    fun forwardConversionAllAffiliations2525D() {
        assertEquals("100110000000000000", cotTypeToSidc("a-u-G", SidcStandard.MIL_STD_2525D).getOrThrow()) // UNKNOWN
        assertEquals("100310000000000000", cotTypeToSidc("a-f-G", SidcStandard.MIL_STD_2525D).getOrThrow()) // FRIENDLY
        assertEquals("100610000000000000", cotTypeToSidc("a-h-G", SidcStandard.MIL_STD_2525D).getOrThrow()) // HOSTILE
        assertEquals("100410000000000000", cotTypeToSidc("a-n-G", SidcStandard.MIL_STD_2525D).getOrThrow()) // NEUTRAL
        assertEquals("100210000000000000", cotTypeToSidc("a-a-G", SidcStandard.MIL_STD_2525D).getOrThrow()) // ASSUMED_FRIENDLY
        assertEquals("100510000000000000", cotTypeToSidc("a-s-G", SidcStandard.MIL_STD_2525D).getOrThrow()) // SUSPECT
        assertEquals("100510000000000000", cotTypeToSidc("a-j-G", SidcStandard.MIL_STD_2525D).getOrThrow()) // JOKER → same code as SUSPECT (5)
        assertEquals("100510000000000000", cotTypeToSidc("a-k-G", SidcStandard.MIL_STD_2525D).getOrThrow()) // FAKER → same code as SUSPECT (5)
    }

    @Test
    fun forwardConversionAllDimensions2525D() {
        // All 7 dimensions in 2525D. Note: AIR, GROUND, and OTHER all use symbolSet "10"
        // because BattleDimension.AIR, GROUND, and OTHER are all defined with symbolSet = "10".
        // Their encoded strings are therefore identical for non-hostile, no-function inputs.
        assertEquals("100305000000000000", cotTypeToSidc("a-f-P", SidcStandard.MIL_STD_2525D).getOrThrow()) // SPACE
        assertEquals("100310000000000000", cotTypeToSidc("a-f-A", SidcStandard.MIL_STD_2525D).getOrThrow()) // AIR  (symbolSet=10 — same as GROUND/OTHER)
        assertEquals("100310000000000000", cotTypeToSidc("a-f-G", SidcStandard.MIL_STD_2525D).getOrThrow()) // GROUND (symbolSet=10)
        assertEquals("100330000000000000", cotTypeToSidc("a-f-S", SidcStandard.MIL_STD_2525D).getOrThrow()) // SEA_SURFACE
        assertEquals("100335000000000000", cotTypeToSidc("a-f-U", SidcStandard.MIL_STD_2525D).getOrThrow()) // SUBSURFACE
        assertEquals("100327000000000000", cotTypeToSidc("a-f-F", SidcStandard.MIL_STD_2525D).getOrThrow()) // SOF
        assertEquals("100310000000000000", cotTypeToSidc("a-f-X", SidcStandard.MIL_STD_2525D).getOrThrow()) // OTHER (symbolSet=10 — same as AIR/GROUND)
    }

    @Test
    fun forwardConversionCrossProductSamples() {
        // Spot-check additional cross-product combinations not covered by affiliation-only or dimension-only tests.
        // Unknown affiliation + SEA_SURFACE (was in the prior fixture table).
        assertEquals("SUSP-----------", cotTypeToSidc("a-u-S", SidcStandard.MIL_STD_2525C).getOrThrow())
        assertEquals("100130000000000000", cotTypeToSidc("a-u-S", SidcStandard.MIL_STD_2525D).getOrThrow())
        // Hostile + SEA_SURFACE
        assertEquals("SHSP-----------", cotTypeToSidc("a-h-S", SidcStandard.MIL_STD_2525C).getOrThrow())
        assertEquals("100630000000000000", cotTypeToSidc("a-h-S", SidcStandard.MIL_STD_2525D).getOrThrow())
    }

    @Test
    fun reverseConversionAllAffiliations2525C() {
        assertEquals("a-u-G", sidcToCotType("SUGP-----------").getOrThrow()) // UNKNOWN
        assertEquals("a-f-G", sidcToCotType("SFGP-----------").getOrThrow()) // FRIENDLY
        assertEquals("a-h-G", sidcToCotType("SHGP-----------").getOrThrow()) // HOSTILE
        assertEquals("a-n-G", sidcToCotType("SNGP-----------").getOrThrow()) // NEUTRAL
        assertEquals("a-a-G", sidcToCotType("SAGP-----------").getOrThrow()) // ASSUMED_FRIENDLY
        assertEquals("a-s-G", sidcToCotType("SSGP-----------").getOrThrow()) // SUSPECT
        // JOKER (J) and FAKER (K) have distinct 2525C codes — round-trip is exact
        assertEquals("a-j-G", sidcToCotType("SJGP-----------").getOrThrow()) // JOKER
        assertEquals("a-k-G", sidcToCotType("SKGP-----------").getOrThrow()) // FAKER
    }

    @Test
    fun reverseConversionAllAffiliations2525D() {
        assertEquals("a-u-G", sidcToCotType("100110000000000000").getOrThrow()) // UNKNOWN (1)
        assertEquals("a-f-G", sidcToCotType("100310000000000000").getOrThrow()) // FRIENDLY (3)
        // HOSTILE + symbolSet "10" + no function → dimension resolves to AIR via heuristic.
        // There is no way to distinguish hostile-ground from hostile-air in a bare 2525D symbolSet "10" SIDC.
        assertEquals("a-h-A", sidcToCotType("100610000000000000").getOrThrow()) // HOSTILE (6) → AIR via heuristic
        assertEquals("a-n-G", sidcToCotType("100410000000000000").getOrThrow()) // NEUTRAL (4)
        assertEquals("a-a-G", sidcToCotType("100210000000000000").getOrThrow()) // ASSUMED_FRIENDLY (2)
        // Affiliation code "5" is shared by SUSPECT, JOKER, and FAKER.
        // fromSidc2525D("5") returns the first enum entry with that code — which is SUSPECT.
        // JOKER and FAKER are not recoverable from a 2525D SIDC alone; this is a known limitation.
        assertEquals("a-s-G", sidcToCotType("100510000000000000").getOrThrow()) // resolves to SUSPECT
    }

    @Test
    fun reverseConversionAllDimensions2525C() {
        // All 7 dimensions in 2525C — each has a distinct sidc2525C code so all are lossless.
        assertEquals("a-f-P", sidcToCotType("SFPP-----------").getOrThrow())  // SPACE
        assertEquals("a-f-A", sidcToCotType("SFAPAA---------").getOrThrow())  // AIR (functionId AA----)
        assertEquals("a-f-G", sidcToCotType("SFGP-----------").getOrThrow())  // GROUND
        assertEquals("a-f-S", sidcToCotType("SFSP-----------").getOrThrow())  // SEA_SURFACE
        assertEquals("a-f-U", sidcToCotType("SFUP-----------").getOrThrow())  // SUBSURFACE
        assertEquals("a-f-F", sidcToCotType("SFFP-----------").getOrThrow())  // SOF
        assertEquals("a-f-X", sidcToCotType("SFXP-----------").getOrThrow())  // OTHER
    }

    @Test
    fun roundTrips2525CExact() {
        // These COT types survive a full 2525C encode → decode cycle exactly.
        // AIR is included here because 2525C uses distinct functionId "AA----" for no-function air,
        // making the round-trip lossless.
        val lossless2525C = listOf(
            "a-u-G", "a-f-G", "a-h-G", "a-n-G", "a-a-G", "a-s-G", "a-j-G", "a-k-G",
            "a-f-P", "a-f-A", "a-f-S", "a-f-U", "a-f-F",
            "a-f-G-U-C",   // with function + modifier
            "a-h-A-M-F",   // hostile air with function + modifier
        )
        lossless2525C.forEach { cotType ->
            assertEquals(
                cotType,
                sidcToCotType(cotTypeToSidc(cotType, SidcStandard.MIL_STD_2525C).getOrThrow()).getOrThrow(),
                "2525C round-trip failed for $cotType",
            )
        }
    }

    @Test
    fun roundTrips2525DLossyAirDimensionIsDocumented() {
        // 2525D uses symbolSet "10" for both AIR and GROUND. The decoder resolves ambiguity
        // using an affiliation heuristic: HOSTILE → AIR, all others → GROUND.
        // Consequence: only hostile-air round-trips losslessly through 2525D.
        // Friendly/neutral/unknown air degrades to GROUND on decode — this is expected and documented.
        val lossless2525D = listOf(
            "a-u-G", "a-f-G", "a-n-G", "a-a-G", "a-s-G",
            "a-f-P", "a-f-S", "a-f-U", "a-f-F",
            "a-h-A",           // hostile air → lossless via heuristic (HOSTILE + symbolSet "10" → AIR)
            "a-f-G-U-C",       // ground with function+modifier
        )
        lossless2525D.forEach { cotType ->
            assertEquals(
                cotType,
                sidcToCotType(cotTypeToSidc(cotType, SidcStandard.MIL_STD_2525D).getOrThrow()).getOrThrow(),
                "2525D round-trip failed for $cotType",
            )
        }

        // Documented lossy cases — friendly/neutral/unknown air encodes to GROUND on 2525D decode.
        assertEquals("a-f-G", sidcToCotType(cotTypeToSidc("a-f-A", SidcStandard.MIL_STD_2525D).getOrThrow()).getOrThrow())
        assertEquals("a-n-G", sidcToCotType(cotTypeToSidc("a-n-A", SidcStandard.MIL_STD_2525D).getOrThrow()).getOrThrow())
        // Hostile ground also degrades: HOSTILE + symbolSet "10" heuristic maps to AIR, not GROUND.
        assertEquals("a-h-A", sidcToCotType(cotTypeToSidc("a-h-G", SidcStandard.MIL_STD_2525D).getOrThrow()).getOrThrow())
    }

    @Test
    fun rejectsInvalidInputs() {
        // Unrecognized affiliation code
        assertTrue(cotTypeToSidc("a-z-G", SidcStandard.MIL_STD_2525C).isFailure)
        // Unrecognized dimension code
        assertTrue(cotTypeToSidc("a-f-Q", SidcStandard.MIL_STD_2525C).isFailure)
        // Too few segments
        assertTrue(cotTypeToSidc("a-f",   SidcStandard.MIL_STD_2525C).isFailure)
        // Too many segments (6)
        assertTrue(cotTypeToSidc("a-f-G-U-C-X-Y", SidcStandard.MIL_STD_2525C).isFailure)
        // SIDC too short for either standard
        assertTrue(sidcToCotType("TOOSHORT").isFailure)
        // Empty SIDC
        assertTrue(sidcToCotType("").isFailure)
        // SIDC with unrecognized affiliation
        assertTrue(sidcToCotType("SXGP-----------").isFailure)
        // Unknown affiliation in a 2525B SIDC
        assertTrue(sidcToCotType("SXAP-----------", SidcStandard.MIL_STD_2525B).isFailure)
        // Unknown dimension in a 2525B SIDC
        assertTrue(sidcToCotType("SFZP-----------", SidcStandard.MIL_STD_2525B).isFailure)
    }

    @Test
    fun forwardConversionAllAffiliations2525B() {
        // All 8 affiliations × GROUND in 2525B.
        // GROUND is unaffected by the AIR-handling difference, so results are identical to 2525C.
        assertEquals("SUGP-----------", cotTypeToSidc("a-u-G", SidcStandard.MIL_STD_2525B).getOrThrow()) // UNKNOWN
        assertEquals("SFGP-----------", cotTypeToSidc("a-f-G", SidcStandard.MIL_STD_2525B).getOrThrow()) // FRIENDLY
        assertEquals("SHGP-----------", cotTypeToSidc("a-h-G", SidcStandard.MIL_STD_2525B).getOrThrow()) // HOSTILE
        assertEquals("SNGP-----------", cotTypeToSidc("a-n-G", SidcStandard.MIL_STD_2525B).getOrThrow()) // NEUTRAL
        assertEquals("SAGP-----------", cotTypeToSidc("a-a-G", SidcStandard.MIL_STD_2525B).getOrThrow()) // ASSUMED_FRIENDLY
        assertEquals("SSGP-----------", cotTypeToSidc("a-s-G", SidcStandard.MIL_STD_2525B).getOrThrow()) // SUSPECT
        assertEquals("SJGP-----------", cotTypeToSidc("a-j-G", SidcStandard.MIL_STD_2525B).getOrThrow()) // JOKER
        assertEquals("SKGP-----------", cotTypeToSidc("a-k-G", SidcStandard.MIL_STD_2525B).getOrThrow()) // FAKER
    }

    @Test
    fun forwardConversionAllDimensions2525B() {
        // All 7 dimensions in 2525B, friendly affiliation.
        // KEY: AIR with no function/modifier must produce "SFAP-----------" (uses "------"), NOT "SFAPAA---------".
        assertEquals("SFPP-----------", cotTypeToSidc("a-f-P", SidcStandard.MIL_STD_2525B).getOrThrow()) // SPACE
        assertEquals("SFAP-----------", cotTypeToSidc("a-f-A", SidcStandard.MIL_STD_2525B).getOrThrow()) // AIR — "------" not "AA----"
        assertEquals("SFGP-----------", cotTypeToSidc("a-f-G", SidcStandard.MIL_STD_2525B).getOrThrow()) // GROUND
        assertEquals("SFSP-----------", cotTypeToSidc("a-f-S", SidcStandard.MIL_STD_2525B).getOrThrow()) // SEA_SURFACE
        assertEquals("SFUP-----------", cotTypeToSidc("a-f-U", SidcStandard.MIL_STD_2525B).getOrThrow()) // SUBSURFACE
        assertEquals("SFFP-----------", cotTypeToSidc("a-f-F", SidcStandard.MIL_STD_2525B).getOrThrow()) // SOF
        assertEquals("SFXP-----------", cotTypeToSidc("a-f-X", SidcStandard.MIL_STD_2525B).getOrThrow()) // OTHER
    }

    @Test
    fun reverseConversionAllAffiliations2525B() {
        // Decode all 8 affiliations from 15-char 2525B SIDCs.
        // Affiliation letter codes are identical to 2525C.
        assertEquals("a-u-G", sidcToCotType("SUGP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // UNKNOWN
        assertEquals("a-f-G", sidcToCotType("SFGP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // FRIENDLY
        assertEquals("a-h-G", sidcToCotType("SHGP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // HOSTILE
        assertEquals("a-n-G", sidcToCotType("SNGP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // NEUTRAL
        assertEquals("a-a-G", sidcToCotType("SAGP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // ASSUMED_FRIENDLY
        assertEquals("a-s-G", sidcToCotType("SSGP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // SUSPECT
        assertEquals("a-j-G", sidcToCotType("SJGP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // JOKER
        assertEquals("a-k-G", sidcToCotType("SKGP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // FAKER
    }

    @Test
    fun reverseConversionAllDimensions2525B() {
        // "SFAP-----------" (all dashes in functionId) decodes to "a-f-A" — no sentinel check in 2525B.
        // "SFAPAA---------" decodes to "a-f-A-A-A" — AA at positions 5-6 is function='A', modifier='A' in 2525B.
        assertEquals("a-f-P", sidcToCotType("SFPP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // SPACE
        assertEquals("a-f-A", sidcToCotType("SFAP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // AIR no-function
        assertEquals("a-f-A-A-A", sidcToCotType("SFAPAA---------", SidcStandard.MIL_STD_2525B).getOrThrow()) // AIR + function=A, modifier=A
        assertEquals("a-f-G", sidcToCotType("SFGP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // GROUND
        assertEquals("a-f-S", sidcToCotType("SFSP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // SEA_SURFACE
        assertEquals("a-f-U", sidcToCotType("SFUP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // SUBSURFACE
        assertEquals("a-f-F", sidcToCotType("SFFP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // SOF
        assertEquals("a-f-X", sidcToCotType("SFXP-----------", SidcStandard.MIL_STD_2525B).getOrThrow()) // OTHER
    }

    @Test
    fun roundTrips2525BExact() {
        // Full encode → decode round-trip for 2525B.
        // Covers all 8 affiliations on GROUND, all 7 dims on friendly, and with-function/modifier cases.
        val lossless2525B = listOf(
            "a-u-G", "a-f-G", "a-h-G", "a-n-G", "a-a-G", "a-s-G", "a-j-G", "a-k-G",
            "a-f-P", "a-f-A", "a-f-S", "a-f-U", "a-f-F", "a-f-X",
            "a-f-G-U-C",   // ground + function + modifier
            "a-h-A-M-F",   // hostile air + function + modifier
        )
        lossless2525B.forEach { cotType ->
            assertEquals(
                cotType,
                sidcToCotType(
                    cotTypeToSidc(cotType, SidcStandard.MIL_STD_2525B).getOrThrow(),
                    SidcStandard.MIL_STD_2525B,
                ).getOrThrow(),
                "2525B round-trip failed for $cotType",
            )
        }
    }

    @Test
    fun airHandlingDiffers2525BVs2525C() {
        // Documents and asserts that 2525B and 2525C encode/decode AIR differently.

        // Forward: "a-f-A" (no function) — 2525B uses "------", 2525C uses "AA----" sentinel
        assertEquals("SFAP-----------", cotTypeToSidc("a-f-A", SidcStandard.MIL_STD_2525B).getOrThrow())
        assertEquals("SFAPAA---------", cotTypeToSidc("a-f-A", SidcStandard.MIL_STD_2525C).getOrThrow())

        // Reverse: "SFAPAA---------" — 2525B treats AA as real function+modifier, 2525C treats it as sentinel
        assertEquals("a-f-A-A-A", sidcToCotType("SFAPAA---------", SidcStandard.MIL_STD_2525B).getOrThrow())
        assertEquals("a-f-A",   sidcToCotType("SFAPAA---------", SidcStandard.MIL_STD_2525C).getOrThrow())

        // 2525B AIR no-function SIDC decodes to clean "a-f-A"
        assertEquals("a-f-A", sidcToCotType("SFAP-----------", SidcStandard.MIL_STD_2525B).getOrThrow())

        // GROUND is identical in B and C
        assertEquals(
            cotTypeToSidc("a-f-G", SidcStandard.MIL_STD_2525B).getOrThrow(),
            cotTypeToSidc("a-f-G", SidcStandard.MIL_STD_2525C).getOrThrow(),
        )
    }
}
