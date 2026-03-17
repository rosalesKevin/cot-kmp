package cot

import cot.parser.CotParser
import cot.serializer.CotSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CotSerializerTest {

    @Test
    fun roundTripsSampleA() {
        val parsed = CotParser.parse(SAMPLE_A).getOrThrow()
        val serialized = CotSerializer.serialize(parsed).getOrThrow()
        val reparsed = CotParser.parse(serialized).getOrThrow()

        assertEquals(parsed.uid, reparsed.uid)
        assertEquals(parsed.type, reparsed.type)
        assertEquals(parsed.point.lat, reparsed.point.lat)
        assertEquals(parsed.point.lon, reparsed.point.lon)
        assertEquals(parsed.detail?.callsign, reparsed.detail?.callsign)
    }

    @Test
    fun roundTripsSampleB() {
        val parsed = CotParser.parse(SAMPLE_B).getOrThrow()
        val serialized = CotSerializer.serialize(parsed).getOrThrow()
        val reparsed = CotParser.parse(serialized).getOrThrow()

        assertEquals(parsed, reparsed)
    }

    @Test
    fun roundTripsSampleC() {
        val parsed = CotParser.parse(SAMPLE_C).getOrThrow()
        val serialized = CotSerializer.serialize(parsed).getOrThrow()
        val reparsed = CotParser.parse(serialized).getOrThrow()

        assertEquals(parsed, reparsed)
    }

    @Test
    fun includesRequiredXmlStructure() {
        val event = CotParser.parse(SAMPLE_A).getOrThrow()
        val serialized = CotSerializer.serialize(event).getOrThrow()

        assertTrue(serialized.contains("""uid="UNIT-001""""))
        assertTrue(serialized.contains("""type="a-f-G-U-C""""))
        assertTrue(serialized.contains("""lat="34.0522""""))
        assertTrue(serialized.contains("<detail>"))
        assertTrue(serialized.contains("</event>"))
    }

    @Test
    fun roundTripsSampleDPreservesRawXml() {
        val parsed   = CotParser.parse(SAMPLE_D).getOrThrow()
        val xml      = CotSerializer.serialize(parsed).getOrThrow()
        val reparsed = CotParser.parse(xml).getOrThrow()

        assertEquals(parsed.uid,              reparsed.uid)
        assertEquals(parsed.detail?.callsign, reparsed.detail?.callsign)
        // rawXml must survive serialize → re-parse unchanged
        assertEquals(parsed.detail?.rawXml,   reparsed.detail?.rawXml)
    }

    @Test
    fun roundTripsSampleEPreservesEntities() {
        val parsed   = CotParser.parse(SAMPLE_E).getOrThrow()
        val xml      = CotSerializer.serialize(parsed).getOrThrow()
        val reparsed = CotParser.parse(xml).getOrThrow()

        assertEquals(parsed.uid,              reparsed.uid)
        // Unescaped values must round-trip correctly
        assertEquals(parsed.detail?.callsign, reparsed.detail?.callsign)
        assertEquals(parsed.detail?.remarks,  reparsed.detail?.remarks)
    }
}
