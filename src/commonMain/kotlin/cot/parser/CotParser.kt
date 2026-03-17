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

package cot.parser

import cot.model.CotDetail
import cot.model.CotEvent
import cot.model.CotPoint
import cot.util.CotTimeUtil

private val eventTagPattern = Regex("""<event\b([^>]*)>""", RegexOption.IGNORE_CASE)
private val pointTagPattern = Regex("""<point\b([^>]*)/?>""", RegexOption.IGNORE_CASE)
private val detailPattern = Regex("""<detail\b[^>]*>([\s\S]*?)</detail>""", RegexOption.IGNORE_CASE)
private val contactPattern = Regex("""<contact\b([^>]*)/?>""", RegexOption.IGNORE_CASE)
private val remarksPattern = Regex("""<remarks\b[^>]*>([\s\S]*?)</remarks>""", RegexOption.IGNORE_CASE)
private val childElementPattern = Regex(
    """<([A-Za-z0-9:_-]+)\b[^>]*>[\s\S]*?</\1>|<([A-Za-z0-9:_-]+)\b[^>]*/>""",
    RegexOption.IGNORE_CASE,
)
private val attributePattern = Regex("""([A-Za-z_:][-A-Za-z0-9_:.]*)\s*=\s*("([^"]*)"|'([^']*)')""")

/**
 * Parses raw COT XML strings into immutable [CotEvent] values.
 */
object CotParser {

    /**
     * Parses a COT XML payload into a [CotEvent].
     */
    fun parse(xml: String): Result<CotEvent> = runCatching {
        require(xml.isNotBlank()) {
            "COT XML string must not be blank."
        }

        val eventAttributes = parseAttributes(
            eventTagPattern.find(xml)?.groupValues?.get(1)
                ?: throw IllegalArgumentException("Missing root <event> element in COT XML.")
        )
        val uid = eventAttributes.requireAttribute("uid", "event")
        val type = eventAttributes.requireAttribute("type", "event")
        val time = CotTimeUtil.requireUtcTimestamp(eventAttributes.requireAttribute("time", "event"), "time")
        val start = CotTimeUtil.requireUtcTimestamp(eventAttributes.requireAttribute("start", "event"), "start")
        val stale = CotTimeUtil.requireUtcTimestamp(eventAttributes.requireAttribute("stale", "event"), "stale")
        val how = eventAttributes.requireAttribute("how", "event")

        val pointAttributes = parseAttributes(
            pointTagPattern.find(xml)?.groupValues?.get(1)
                ?: throw IllegalArgumentException("Missing required <point> element in <event>.")
        )
        val point = CotPoint(
            lat = pointAttributes.requireDoubleAttribute("lat", "point"),
            lon = pointAttributes.requireDoubleAttribute("lon", "point"),
            hae = pointAttributes.optionalDoubleAttribute("hae") ?: 0.0,
            ce = pointAttributes.optionalDoubleAttribute("ce") ?: CotPoint.UNKNOWN_ERROR,
            le = pointAttributes.optionalDoubleAttribute("le") ?: CotPoint.UNKNOWN_ERROR,
        )

        val detail = detailPattern.find(xml)?.groupValues?.get(1)?.let(::parseDetail)

        CotEvent(
            uid = uid,
            type = type,
            time = time,
            start = start,
            stale = stale,
            how = how,
            point = point,
            detail = detail,
        )
    }

    private fun parseDetail(detailXml: String): CotDetail {
        val contactAttributes = contactPattern.find(detailXml)?.groupValues?.get(1)?.let(::parseAttributes)
        val remarks = remarksPattern.find(detailXml)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotEmpty() }?.unescapeXml()

        val unknownChildren = childElementPattern.findAll(detailXml)
            .map { it.value.trim() }
            .filterNot { child ->
                child.startsWith("<contact", ignoreCase = true) || child.startsWith("<remarks", ignoreCase = true)
            }
            .joinToString(separator = "")
            .takeIf { it.isNotEmpty() }

        return CotDetail(
            callsign = contactAttributes?.get("callsign")?.unescapeXml(),
            phone = contactAttributes?.get("phone")?.unescapeXml(),
            remarks = remarks,
            rawXml = unknownChildren,
        )
    }
}

private fun parseAttributes(rawAttributes: String): Map<String, String> =
    attributePattern.findAll(rawAttributes)
        .associate { match ->
            val value = match.groups[3]?.value ?: match.groups[4]?.value.orEmpty()
            match.groupValues[1] to value
        }

private fun Map<String, String>.requireAttribute(name: String, element: String): String =
    this[name]?.takeIf { it.isNotBlank() } ?: throw IllegalArgumentException(
        "Missing required attribute '$name' on <$element> element."
    )

private fun Map<String, String>.requireDoubleAttribute(name: String, element: String): Double =
    this[name]?.toDoubleOrNull() ?: throw IllegalArgumentException(
        "Expected numeric attribute '$name' on <$element> element. Got '${this[name]}'."
    )

private fun Map<String, String>.optionalDoubleAttribute(name: String): Double? =
    this[name]?.toDoubleOrNull()

private fun String.unescapeXml(): String = this
    .replace("&quot;", "\"")
    .replace("&gt;", ">")
    .replace("&lt;", "<")
    .replace("&amp;", "&")
