/*
 * Copyright (c) 2025 Kevin Klein Rosales
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

/**
 * Serializes [CotEvent] values into COT XML strings.
 */
object CotSerializer {

    /**
     * Serializes a [CotEvent] into a COT XML payload.
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

            append(
                "<point lat=\"${event.point.lat}\" lon=\"${event.point.lon}\" " +
                    "hae=\"${event.point.hae}\" ce=\"${event.point.ce}\" le=\"${event.point.le}\"/>"
            )

            event.detail?.let { detail ->
                append(serializeDetail(detail))
            }

            append("</event>")
        }
    }
}

private fun serializeDetail(detail: CotDetail): String = buildString {
    append("<detail>")
    if (detail.callsign != null || detail.phone != null) {
        append("<contact")
        detail.callsign?.let { append(" callsign=\"${it.escapeXml()}\"") }
        detail.phone?.let { append(" phone=\"${it.escapeXml()}\"") }
        append("/>")
    }
    detail.remarks?.let { append("<remarks>${it.escapeXml()}</remarks>") }
    detail.rawXml?.let { append(it) }
    append("</detail>")
}

private fun String.escapeXml(): String = this
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
