package cot

import cot.model.CotDetail
import cot.model.CotEvent
import cot.model.CotPoint
import cot.model.DetailElement
import cot.parser.CotParser
import cot.serializer.CotSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CotSerializerTest {

    @Test
    fun roundTripsSampleA() {
        val parsed     = CotParser.parse(SAMPLE_A).getOrThrow()
        val serialized = CotSerializer.serialize(parsed).getOrThrow()
        val reparsed   = CotParser.parse(serialized).getOrThrow()

        assertEquals(parsed.uid,              reparsed.uid)
        assertEquals(parsed.type,             reparsed.type)
        assertEquals(parsed.point.lat,        reparsed.point.lat)
        assertEquals(parsed.point.lon,        reparsed.point.lon)
        assertEquals(parsed.detail?.callsign, reparsed.detail?.callsign)
    }

    @Test
    fun roundTripsSampleB() {
        val parsed     = CotParser.parse(SAMPLE_B).getOrThrow()
        val serialized = CotSerializer.serialize(parsed).getOrThrow()
        val reparsed   = CotParser.parse(serialized).getOrThrow()

        assertEquals(parsed, reparsed)
    }

    @Test
    fun roundTripsSampleC() {
        val parsed     = CotParser.parse(SAMPLE_C).getOrThrow()
        val serialized = CotSerializer.serialize(parsed).getOrThrow()
        val reparsed   = CotParser.parse(serialized).getOrThrow()

        assertEquals(parsed, reparsed)
    }

    @Test
    fun includesRequiredXmlStructure() {
        val event      = CotParser.parse(SAMPLE_A).getOrThrow()
        val serialized = CotSerializer.serialize(event).getOrThrow()

        assertTrue(serialized.contains("""uid="UNIT-001""""))
        assertTrue(serialized.contains("""type="a-f-G-U-C""""))
        assertTrue(serialized.contains("""lat="34.0522""""))
        assertTrue(serialized.contains("<detail>"))
        assertTrue(serialized.contains("</event>"))
    }

    @Test
    fun roundTripsSampleDPreservesDetailChildren() {
        val parsed     = CotParser.parse(SAMPLE_D).getOrThrow()
        val xml        = CotSerializer.serialize(parsed).getOrThrow()
        val reparsed   = CotParser.parse(xml).getOrThrow()

        assertEquals(parsed.uid,              reparsed.uid)
        assertEquals(parsed.detail?.callsign, reparsed.detail?.callsign)
        // Structured children must survive serialize → re-parse unchanged
        assertEquals(parsed.detail?.children, reparsed.detail?.children)
    }

    @Test
    fun roundTripsSampleEPreservesEntities() {
        val parsed     = CotParser.parse(SAMPLE_E).getOrThrow()
        val xml        = CotSerializer.serialize(parsed).getOrThrow()
        val reparsed   = CotParser.parse(xml).getOrThrow()

        assertEquals(parsed.uid,             reparsed.uid)
        // Unescaped values must survive the round-trip correctly
        assertEquals(parsed.detail?.callsign, reparsed.detail?.callsign)
        assertEquals(parsed.detail?.remarks,  reparsed.detail?.remarks)
    }

    @Test
    fun serializerEscapesSpecialCharactersInAttributes() {
        // Verify the serializer escapes characters that are illegal in XML attributes
        val event = CotEvent(
            uid   = "uid-with-<>&\"",
            type  = "a-f-G",
            time  = "2024-01-01T00:00:00.000Z",
            start = "2024-01-01T00:00:00.000Z",
            stale = "2024-01-01T01:00:00.000Z",
            how   = "m-g",
            point = CotPoint(lat = 0.0, lon = 0.0),
        )
        val xml      = CotSerializer.serialize(event).getOrThrow()
        val reparsed = CotParser.parse(xml).getOrThrow()

        // The raw special characters must round-trip correctly
        assertEquals("uid-with-<>&\"", reparsed.uid)
    }

    @Test
    fun serializerEscapesSpecialCharactersInRemarks() {
        val detail = CotDetail(remarks = "Speed > 400kts & heading < 090; callsign \"ALPHA\"")
        val event  = CotEvent(
            uid   = "ESC-001",
            type  = "a-f-G",
            time  = "2024-01-01T00:00:00.000Z",
            start = "2024-01-01T00:00:00.000Z",
            stale = "2024-01-01T01:00:00.000Z",
            how   = "m-g",
            point = CotPoint(lat = 0.0, lon = 0.0),
            detail = detail,
        )
        val xml      = CotSerializer.serialize(event).getOrThrow()
        val reparsed = CotParser.parse(xml).getOrThrow()

        assertEquals(detail.remarks, reparsed.detail?.remarks)
    }

    @Test
    fun roundTripsDetailChildrenWithAttributes() {
        // Verify that a manually constructed DetailElement survives a round-trip
        val child  = DetailElement(
            tag        = "status",
            attributes = mapOf("battery" to "95", "readiness" to "true"),
        )
        val detail = CotDetail(callsign = "DELTA-3", children = listOf(child))
        val event  = CotEvent(
            uid    = "RT-001",
            type   = "a-f-G",
            time   = "2024-01-01T00:00:00.000Z",
            start  = "2024-01-01T00:00:00.000Z",
            stale  = "2024-01-01T01:00:00.000Z",
            how    = "m-g",
            point  = CotPoint(lat = 0.0, lon = 0.0),
            detail = detail,
        )
        val xml      = CotSerializer.serialize(event).getOrThrow()
        val reparsed = CotParser.parse(xml).getOrThrow()

        assertNotNull(reparsed.detail)
        val reparsedDetail = requireNotNull(reparsed.detail)
        assertEquals("DELTA-3", reparsedDetail.callsign)
        val reparsedChild = reparsedDetail.children.first()
        assertEquals("status",  reparsedChild.tag)
        assertEquals("95",      reparsedChild.attributes["battery"])
        assertEquals("true",    reparsedChild.attributes["readiness"])
    }

    @Test
    fun roundTripsNestedDetailChildren() {
        val nested = DetailElement(tag = "point", attributes = mapOf("lat" to "34.0", "lon" to "-118.0"))
        val parent = DetailElement(tag = "link",  attributes = mapOf("type" to "a-f-G"), children = listOf(nested))
        val detail = CotDetail(children = listOf(parent))
        val event  = CotEvent(
            uid    = "NESTED-001",
            type   = "a-f-G",
            time   = "2024-01-01T00:00:00.000Z",
            start  = "2024-01-01T00:00:00.000Z",
            stale  = "2024-01-01T01:00:00.000Z",
            how    = "m-g",
            point  = CotPoint(lat = 0.0, lon = 0.0),
            detail = detail,
        )
        val xml      = CotSerializer.serialize(event).getOrThrow()
        val reparsed = CotParser.parse(xml).getOrThrow()

        val reparsedLink   = reparsed.detail!!.children.first { it.tag == "link" }
        val reparsedNested = reparsedLink.children.first { it.tag == "point" }
        assertEquals("34.0",  reparsedNested.attributes["lat"])
        assertEquals("-118.0", reparsedNested.attributes["lon"])
    }
}
