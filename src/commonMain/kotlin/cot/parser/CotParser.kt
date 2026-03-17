/*
 * Copyright (c) 2026 Kevin Klein Rosales
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cot.parser

import cot.model.CotDetail
import cot.model.CotEvent
import cot.model.CotPoint
import cot.model.DetailElement
import cot.util.CotTimeUtil
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.xmlStreaming

/**
 * Parses raw COT XML strings into immutable [CotEvent] values.
 *
 * Uses [xmlStreaming] (pdvrieze/xmlutil) for XML parsing, which correctly handles
 * XML entity escaping/unescaping, self-closing elements, XML declarations, namespaces,
 * and deeply nested structures. Attribute and text values returned from the parser are
 * always XML-unescaped.
 */
object CotParser {

    /**
     * Parses a COT XML payload into a [CotEvent].
     *
     * Returns [Result.failure] for blank input, missing required elements or attributes,
     * invalid timestamp format, and non-numeric coordinate values.
     */
    fun parse(xml: String): Result<CotEvent> = runCatching {
        require(xml.isNotBlank()) { "COT XML string must not be blank." }

        val reader = xmlStreaming.newReader(xml)

        // Advance past the XML declaration and any processing instructions/comments
        // to the root element.
        reader.advanceToFirstStartElement()

        check(reader.localName == "event") {
            "Missing root <event> element in COT XML."
        }

        val uid   = reader.requireAttr("uid",   "event")
        val type  = reader.requireAttr("type",  "event")
        val time  = CotTimeUtil.requireUtcTimestamp(reader.requireAttr("time",  "event"), "time")
        val start = CotTimeUtil.requireUtcTimestamp(reader.requireAttr("start", "event"), "start")
        val stale = CotTimeUtil.requireUtcTimestamp(reader.requireAttr("stale", "event"), "stale")
        val how   = reader.requireAttr("how",   "event")

        var point: CotPoint? = null
        var detail: CotDetail? = null

        while (reader.hasNext()) {
            when (reader.next()) {
                EventType.START_ELEMENT -> when (reader.localName) {
                    "point"  -> point  = reader.parsePoint()
                    "detail" -> detail = reader.parseDetail()
                    else     -> reader.skipCurrentElement()
                }
                EventType.END_ELEMENT -> if (reader.localName == "event") break
                else -> Unit
            }
        }

        requireNotNull(point) { "Missing required <point> element in <event>." }

        CotEvent(uid, type, time, start, stale, how, point, detail)
    }
}

// ── Element parsers ───────────────────────────────────────────────────────────

/**
 * Parses a `<point>` element. The reader must be positioned at START_ELEMENT "point".
 * Advances the reader past the matching END_ELEMENT before returning.
 */
private fun XmlReader.parsePoint(): CotPoint {
    val lat = requireDoubleAttr("lat", "point")
    val lon = requireDoubleAttr("lon", "point")
    val hae = optionalDoubleAttr("hae") ?: 0.0
    val ce  = optionalDoubleAttr("ce")  ?: CotPoint.UNKNOWN_ERROR
    val le  = optionalDoubleAttr("le")  ?: CotPoint.UNKNOWN_ERROR
    skipCurrentElement()
    return CotPoint(lat, lon, hae, ce, le)
}

/**
 * Parses a `<detail>` element. The reader must be positioned at START_ELEMENT "detail".
 * Advances the reader past the matching END_ELEMENT before returning.
 *
 * `<contact>` and `<remarks>` are promoted to first-class fields. All other children
 * are captured as [DetailElement] values in document order.
 */
private fun XmlReader.parseDetail(): CotDetail {
    var callsign: String? = null
    var phone: String?    = null
    var remarks: String?  = null
    val children          = mutableListOf<DetailElement>()

    while (hasNext()) {
        when (next()) {
            EventType.START_ELEMENT -> when (localName) {
                "contact" -> {
                    callsign = getAttributeValue("", "callsign")?.takeIf { it.isNotEmpty() }
                    phone    = getAttributeValue("", "phone")   ?.takeIf { it.isNotEmpty() }
                    skipCurrentElement()
                }
                "remarks" -> remarks = readTextContent().takeIf { it.isNotEmpty() }
                else      -> children.add(readDetailElement())
            }
            EventType.END_ELEMENT -> if (localName == "detail") break
            else -> Unit
        }
    }

    return CotDetail(callsign, phone, remarks, children)
}

/**
 * Recursively reads a single detail child element into a [DetailElement].
 * The reader must be positioned at START_ELEMENT for the target tag.
 * Advances the reader past the matching END_ELEMENT before returning.
 */
private fun XmlReader.readDetailElement(): DetailElement {
    val tag        = localName
    val attributes = buildMap<String, String> {
        for (i in 0 until attributeCount) {
            put(getAttributeLocalName(i), getAttributeValue(i))
        }
    }

    val textBuffer = StringBuilder()
    val children   = mutableListOf<DetailElement>()

    while (hasNext()) {
        when (next()) {
            EventType.START_ELEMENT                   -> children.add(readDetailElement())
            EventType.TEXT, EventType.ENTITY_REF      -> textBuffer.append(text)
            EventType.END_ELEMENT                     -> break
            else                                      -> Unit
        }
    }

    val trimmedText = textBuffer.toString().trim().takeIf { it.isNotEmpty() }
    return DetailElement(tag, attributes, trimmedText, children)
}

// ── Reader helpers ────────────────────────────────────────────────────────────

/**
 * Advances the reader past the XML declaration and any leading processing
 * instructions or comments until positioned at the first START_ELEMENT.
 */
private fun XmlReader.advanceToFirstStartElement() {
    while (hasNext()) {
        if (next() == EventType.START_ELEMENT) return
    }
    throw IllegalArgumentException("Missing root <event> element in COT XML.")
}

/**
 * Reads all text content of the current element until its END_ELEMENT, returning
 * the trimmed result. XML entities are unescaped by the underlying parser.
 */
private fun XmlReader.readTextContent(): String {
    val sb = StringBuilder()
    while (hasNext()) {
        when (next()) {
            EventType.TEXT, EventType.ENTITY_REF -> sb.append(text)
            EventType.END_ELEMENT                -> break
            else                                 -> Unit
        }
    }
    return sb.toString().trim()
}

/**
 * Skips the current element and all its descendants, leaving the reader just
 * past the matching END_ELEMENT.
 */
private fun XmlReader.skipCurrentElement() {
    var depth = 1
    while (hasNext() && depth > 0) {
        when (next()) {
            EventType.START_ELEMENT -> depth++
            EventType.END_ELEMENT   -> depth--
            else                    -> Unit
        }
    }
}

private fun XmlReader.requireAttr(name: String, element: String): String =
    getAttributeValue("", name)?.takeIf { it.isNotBlank() }
        ?: throw IllegalArgumentException(
            "Missing required attribute '$name' on <$element> element."
        )

private fun XmlReader.requireDoubleAttr(name: String, element: String): Double {
    val raw = getAttributeValue("", name)
    return raw?.toDoubleOrNull()
        ?: throw IllegalArgumentException(
            "Expected numeric attribute '$name' on <$element> element. Got '$raw'."
        )
}

private fun XmlReader.optionalDoubleAttr(name: String): Double? =
    getAttributeValue("", name)?.toDoubleOrNull()
