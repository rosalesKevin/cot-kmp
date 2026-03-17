package cot

import cot.model.CotPoint
import cot.parser.CotParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CotParserTest {

    @Test
    fun parsesSampleA() {
        val result = CotParser.parse(SAMPLE_A)

        assertTrue(result.isSuccess)
        val event = result.getOrThrow()
        assertEquals("UNIT-001", event.uid)
        assertEquals("a-f-G-U-C", event.type)
        assertEquals(34.0522, event.point.lat)
        assertEquals(-118.2437, event.point.lon)
        assertEquals(71.0, event.point.hae)
        assertEquals(10.0, event.point.ce)
        assertEquals(5.0, event.point.le)
        assertNotNull(event.detail)
        val detail = requireNotNull(event.detail)
        assertEquals("ALPHA-1", detail.callsign)
        assertEquals("555-0100", detail.phone)
        assertEquals("Patrol unit, sector 4", detail.remarks)
    }

    @Test
    fun parsesSampleBWithoutDetail() {
        val result = CotParser.parse(SAMPLE_B)

        assertTrue(result.isSuccess)
        val event = result.getOrThrow()
        assertEquals("TRACK-002", event.uid)
        assertNull(event.detail)
        assertEquals(35.6762, event.point.lat)
    }

    @Test
    fun parsesSampleCWithUnknownErrors() {
        val result = CotParser.parse(SAMPLE_C)

        assertTrue(result.isSuccess)
        val event = result.getOrThrow()
        assertEquals("VESSEL-003", event.uid)
        assertEquals(CotPoint.UNKNOWN_ERROR, event.point.ce)
        assertEquals(CotPoint.UNKNOWN_ERROR, event.point.le)
    }

    @Test
    fun rejectsBlankInput() {
        val result = CotParser.parse("")

        assertTrue(result.isFailure)
    }

    @Test
    fun rejectsMissingPoint() {
        val result = CotParser.parse(
            """
            <event uid="x" type="a-f-G" time="2024-01-01T00:00:00.000Z" start="2024-01-01T00:00:00.000Z"
              stale="2024-01-01T01:00:00.000Z" how="m-g"/>
            """.trimIndent(),
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun rejectsMissingRequiredAttribute() {
        val result = CotParser.parse(
            """
            <event uid="x">
              <point lat="0" lon="0"/>
            </event>
            """.trimIndent(),
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun parsesSampleDWithAtakDetail() {
        val result = CotParser.parse(SAMPLE_D)

        assertTrue(result.isSuccess)
        val event = result.getOrThrow()
        assertEquals("ATAK-001", event.uid)
        assertEquals("a-f-G-U-C-I", event.type)
        assertEquals(40.7128, event.point.lat)
        assertEquals(-74.0060, event.point.lon)
        assertNotNull(event.detail)
        val detail = requireNotNull(event.detail)
        assertEquals("BRAVO-2", detail.callsign)
        assertEquals("555-0200", detail.phone)
        // Vendor-specific children are preserved as structured DetailElement values
        assertTrue(detail.children.isNotEmpty(), "detail.children must not be empty")
        assertTrue(detail.children.any { it.tag == "__group" }, "children must preserve <__group>")
        assertTrue(detail.children.any { it.tag == "takv" },    "children must preserve <takv>")
        assertTrue(detail.children.any { it.tag == "status" },  "children must preserve <status>")
    }

    @Test
    fun parsesSampleDDetailAttributes() {
        // Verify that DetailElement attributes are correctly captured
        val event  = CotParser.parse(SAMPLE_D).getOrThrow()
        val detail = requireNotNull(event.detail)
        val group  = detail.children.first { it.tag == "__group" }
        assertEquals("Cyan",        group.attributes["name"])
        assertEquals("Team Member", group.attributes["role"])

        val takv = detail.children.first { it.tag == "takv" }
        assertEquals("Samsung SM-G991B", takv.attributes["device"])
        assertEquals("ATAK-CIV",         takv.attributes["platform"])
        assertEquals("4.7.0.8",          takv.attributes["version"])
    }

    @Test
    fun parsesSampleEWithXmlEntities() {
        val result = CotParser.parse(SAMPLE_E)

        assertTrue(result.isSuccess)
        val event = result.getOrThrow()
        assertEquals("HOSTILE-002", event.uid)
        val detail  = requireNotNull(event.detail)
        // xmlutil automatically unescapes &amp; → &
        assertEquals("AT&T-TRACK", detail.callsign)
        val remarks = requireNotNull(detail.remarks)
        assertTrue(remarks.contains(">"),  "remarks must unescape &gt;")
        assertTrue(remarks.contains("<"),  "remarks must unescape &lt;")
        assertTrue(remarks.contains("\""), "remarks must unescape &quot;")
    }

    @Test
    fun parsesSampleFWithXmlDeclaration() {
        val result = CotParser.parse(SAMPLE_F)

        assertTrue(result.isSuccess)
        val event = result.getOrThrow()
        assertEquals("VESSEL-004", event.uid)
        assertEquals("a-n-S", event.type)
        assertNull(event.detail)
    }

    @Test
    fun parsesSampleGWithSelfClosingDetailTag() {
        val result = CotParser.parse(SAMPLE_G)

        assertTrue(result.isSuccess)
        val event = result.getOrThrow()
        assertEquals("EMPTY-DETAIL-001", event.uid)
        // <detail/> is a valid empty detail element — parsed as CotDetail with no children.
        assertNotNull(event.detail)
        val detail = requireNotNull(event.detail)
        assertTrue(detail.children.isEmpty())
        assertNull(detail.callsign)
        assertNull(detail.remarks)
    }

    @Test
    fun rejectsInvalidTimestamp() {
        val result = CotParser.parse(
            """
            <event uid="x" type="a-f-G" time="not-a-timestamp" start="2024-01-01T00:00:00.000Z"
              stale="2024-01-01T01:00:00.000Z" how="m-g">
              <point lat="0.0" lon="0.0"/>
            </event>
            """.trimIndent(),
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("time"))
    }

    @Test
    fun rejectsNonNumericLatitude() {
        val result = CotParser.parse(
            """
            <event uid="x" type="a-f-G" time="2024-01-01T00:00:00.000Z"
              start="2024-01-01T00:00:00.000Z" stale="2024-01-01T01:00:00.000Z" how="m-g">
              <point lat="not-a-number" lon="0.0"/>
            </event>
            """.trimIndent(),
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("lat"))
    }

    @Test
    fun rejectsMissingUid() {
        val result = CotParser.parse(
            """
            <event type="a-f-G" time="2024-01-01T00:00:00.000Z"
              start="2024-01-01T00:00:00.000Z" stale="2024-01-01T01:00:00.000Z" how="m-g">
              <point lat="0.0" lon="0.0"/>
            </event>
            """.trimIndent(),
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("uid"))
    }

    @Test
    fun rejectsTruncatedXml() {
        val result = CotParser.parse("<event uid=\"x\" type=\"a-f-G\"")
        assertTrue(result.isFailure)
    }

    @Test
    fun rejectsXmlWithNoEventTag() {
        val result = CotParser.parse("<root><point lat=\"0\" lon=\"0\"/></root>")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("event"))
    }

    @Test
    fun parsesNestedDetailChildren() {
        // Verify that deeply nested detail elements are captured recursively
        val result = CotParser.parse(SAMPLE_H)

        assertTrue(result.isSuccess)
        val detail      = requireNotNull(result.getOrThrow().detail)
        val link        = detail.children.first { it.tag == "link" }
        assertEquals("a-f-G-U-C", link.attributes["type"])
        // Nested <point> inside <link>
        val nestedPoint = link.children.firstOrNull { it.tag == "point" }
        assertNotNull(nestedPoint)
        assertEquals("34.0522", requireNotNull(nestedPoint).attributes["lat"])
    }

    @Test
    fun parsesAposEntityInAttribute() {
        // Verifies that &apos; (the 5th standard XML entity) is correctly unescaped
        val result = CotParser.parse(SAMPLE_I)

        assertTrue(result.isSuccess)
        val callsign = result.getOrThrow().detail?.callsign
        assertEquals("O'Brien", callsign)
    }
}

// ── Test fixtures ─────────────────────────────────────────────────────────────

internal val SAMPLE_A =
    """
    <event version="2.0" uid="UNIT-001" type="a-f-G-U-C"
      time="2024-01-01T00:00:00.000Z" start="2024-01-01T00:00:00.000Z"
      stale="2024-01-01T01:00:00.000Z" how="m-g">
      <point lat="34.0522" lon="-118.2437" hae="71.0" ce="10.0" le="5.0"/>
      <detail>
        <contact callsign="ALPHA-1" phone="555-0100"/>
        <remarks>Patrol unit, sector 4</remarks>
      </detail>
    </event>
    """.trimIndent()

internal val SAMPLE_B =
    """
    <event version="2.0" uid="TRACK-002" type="a-h-A"
      time="2024-01-01T00:00:00.000Z" start="2024-01-01T00:00:00.000Z"
      stale="2024-01-01T01:00:00.000Z" how="m-g">
      <point lat="35.6762" lon="139.6503" hae="5000.0" ce="50.0" le="100.0"/>
    </event>
    """.trimIndent()

internal val SAMPLE_C =
    """
    <event version="2.0" uid="VESSEL-003" type="a-n-S"
      time="2024-01-01T00:00:00.000Z" start="2024-01-01T00:00:00.000Z"
      stale="2024-01-01T02:00:00.000Z" how="h-g-i-g-o">
      <point lat="21.3069" lon="-157.8583" hae="0.0" ce="9999999.0" le="9999999.0"/>
    </event>
    """.trimIndent()

/** Friendly ground infantry unit with ATAK group/takv/status detail children. */
internal val SAMPLE_D =
    """
    <event version="2.0" uid="ATAK-001" type="a-f-G-U-C-I"
      time="2024-06-15T12:00:00.000Z" start="2024-06-15T12:00:00.000Z"
      stale="2024-06-15T13:00:00.000Z" how="m-g">
      <point lat="40.7128" lon="-74.0060" hae="10.0" ce="5.0" le="2.0"/>
      <detail>
        <contact callsign="BRAVO-2" phone="555-0200"/>
        <__group name="Cyan" role="Team Member"/>
        <takv device="Samsung SM-G991B" platform="ATAK-CIV" os="31" version="4.7.0.8"/>
        <status battery="87"/>
      </detail>
    </event>
    """.trimIndent()

/** Hostile aircraft with XML entities in callsign and remarks. */
internal val SAMPLE_E =
    """
    <event version="2.0" uid="HOSTILE-002" type="a-h-A-M-F"
      time="2024-06-15T08:30:00.000Z" start="2024-06-15T08:30:00.000Z"
      stale="2024-06-15T09:30:00.000Z" how="m-g">
      <point lat="51.5074" lon="-0.1278" hae="3000.0" ce="100.0" le="50.0"/>
      <detail>
        <contact callsign="AT&amp;T-TRACK" phone="555-0300"/>
        <remarks>Speed &gt; 400kts; squawk &lt;7700&gt;; callsign &quot;BANDIT-1&quot;</remarks>
      </detail>
    </event>
    """.trimIndent()

/** Neutral sea-surface vessel — no detail block, XML declaration present. */
internal val SAMPLE_F =
    """
    <?xml version="1.0" encoding="UTF-8"?>
    <event version="2.0" uid="VESSEL-004" type="a-n-S"
      time="2024-06-15T06:00:00.000Z" start="2024-06-15T06:00:00.000Z"
      stale="2024-06-15T18:00:00.000Z" how="h-g-i-g-o">
      <point lat="37.7749" lon="-122.4194" hae="0.0" ce="9999999.0" le="9999999.0"/>
    </event>
    """.trimIndent()

/** COT with self-closing empty <detail/> element. */
internal val SAMPLE_G =
    """
    <event version="2.0" uid="EMPTY-DETAIL-001" type="a-f-G"
      time="2024-06-15T10:00:00.000Z" start="2024-06-15T10:00:00.000Z"
      stale="2024-06-15T11:00:00.000Z" how="m-g">
      <point lat="48.8566" lon="2.3522" hae="35.0" ce="15.0" le="8.0"/>
      <detail/>
    </event>
    """.trimIndent()

/** COT with a nested detail child — <link> containing a <point> child. */
internal val SAMPLE_H =
    """
    <event version="2.0" uid="LINK-001" type="a-f-G-U-C"
      time="2024-06-15T10:00:00.000Z" start="2024-06-15T10:00:00.000Z"
      stale="2024-06-15T11:00:00.000Z" how="m-g">
      <point lat="34.0100" lon="-118.0000" hae="0.0" ce="10.0" le="5.0"/>
      <detail>
        <link type="a-f-G-U-C" relation="p-p">
          <point lat="34.0522" lon="-118.2437"/>
        </link>
      </detail>
    </event>
    """.trimIndent()

/** COT with &apos; XML entity in a contact attribute. */
internal val SAMPLE_I =
    """
    <event version="2.0" uid="APOS-001" type="a-f-G"
      time="2024-06-15T10:00:00.000Z" start="2024-06-15T10:00:00.000Z"
      stale="2024-06-15T11:00:00.000Z" how="m-g">
      <point lat="0.0" lon="0.0"/>
      <detail>
        <contact callsign="O&apos;Brien"/>
      </detail>
    </event>
    """.trimIndent()
