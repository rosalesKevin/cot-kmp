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

package cot.serializer

import cot.model.CotDetail
import cot.model.CotEvent
import cot.model.DetailElement

/**
 * Serializes [CotEvent] values into COT XML strings.
 *
 * Uses a [StringBuilder] with complete XML escaping for all five standard XML entities
 * (`&amp;`, `&lt;`, `&gt;`, `&quot;`, `&apos;`). Attribute values and text content
 * are always escaped before emission so the output is well-formed XML.
 *
 * Note: XML reading uses pdvrieze/xmlutil for robustness (see [cot.parser.CotParser]).
 * Writing uses a direct StringBuilder approach because xmlutil's writer API does not
 * expose a multiplatform-safe `Appendable` overload in its public surface (as of
 * `1.0.0-rc2`). The escaping performed here is equivalent to what a library writer
 * would do.
 */
object CotSerializer {

    /**
     * Serializes a [CotEvent] into a COT XML payload.
     *
     * The output is a single-line, well-formed XML string with no XML declaration,
     * matching the format expected by TAK Server, ATAK, and other COT consumers.
     */
    fun serialize(event: CotEvent): Result<String> = runCatching {
        buildString {
            append("<event version=\"2.0\"")
            append(" uid=\"${event.uid.escapeXml()}\"")
            append(" type=\"${event.type.escapeXml()}\"")
            append(" time=\"${event.time.escapeXml()}\"")
            append(" start=\"${event.start.escapeXml()}\"")
            append(" stale=\"${event.stale.escapeXml()}\"")
            append(" how=\"${event.how.escapeXml()}\">")

            append("<point")
            append(" lat=\"${event.point.lat}\"")
            append(" lon=\"${event.point.lon}\"")
            append(" hae=\"${event.point.hae}\"")
            append(" ce=\"${event.point.ce}\"")
            append(" le=\"${event.point.le}\"")
            append("/>")

            event.detail?.let { append(serializeDetail(it)) }

            append("</event>")
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

private fun serializeDetail(detail: CotDetail): String = buildString {
    append("<detail>")

    if (detail.callsign != null || detail.phone != null) {
        append("<contact")
        detail.callsign?.let { append(" callsign=\"${it.escapeXml()}\"") }
        detail.phone   ?.let { append(" phone=\"${it.escapeXml()}\"") }
        append("/>")
    }

    detail.remarks?.let {
        append("<remarks>")
        append(it.escapeXml())
        append("</remarks>")
    }

    detail.children.forEach { append(serializeDetailElement(it)) }

    append("</detail>")
}

private fun serializeDetailElement(element: DetailElement): String = buildString {
    append("<${element.tag}")
    element.attributes.forEach { (name, value) ->
        append(" $name=\"${value.escapeXml()}\"")
    }
    if (element.text == null && element.children.isEmpty()) {
        append("/>")
    } else {
        append(">")
        element.text?.let { append(it.escapeXml()) }
        element.children.forEach { append(serializeDetailElement(it)) }
        append("</${element.tag}>")
    }
}

/**
 * Escapes all five standard XML special characters so the value is safe
 * to embed in an XML attribute or text node.
 *
 * Order matters: `&` must be replaced first to avoid double-escaping.
 */
private fun String.escapeXml(): String = this
    .replace("&",  "&amp;")
    .replace("<",  "&lt;")
    .replace(">",  "&gt;")
    .replace("\"", "&quot;")
    .replace("'",  "&apos;")
