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

@file:OptIn(ExperimentalJsExport::class)

package cot

import cot.model.CotDetail
import cot.model.CotEvent
import cot.model.CotPoint
import cot.parser.CotParser
import cot.serializer.CotSerializer
import cot.sidc.SidcStandard
import cot.sidc.cotTypeToSidc
import cot.sidc.sidcToCotType
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@JsExport
enum class JsSidcStandard {
    MIL_STD_2525B,
    MIL_STD_2525C,
    MIL_STD_2525D,
}

@JsExport
data class JsCotPoint(
    val lat: Double,
    val lon: Double,
    val hae: Double = 0.0,
    val ce: Double = CotPoint.UNKNOWN_ERROR,
    val le: Double = CotPoint.UNKNOWN_ERROR,
)

@JsExport
data class JsCotDetail(
    val callsign: String? = null,
    val phone: String? = null,
    val remarks: String? = null,
)

@JsExport
data class JsCotEvent(
    val uid: String,
    val type: String,
    val time: String,
    val start: String,
    val stale: String,
    val how: String,
    val point: JsCotPoint,
    val detail: JsCotDetail? = null,
)

@JsExport
data class JsCotEventResult(
    val ok: Boolean,
    val value: JsCotEvent? = null,
    val error: String? = null,
)

@JsExport
data class JsStringResult(
    val ok: Boolean,
    val value: String? = null,
    val error: String? = null,
)

@JsExport
object CotJsApi {
    fun parse(xml: String): JsCotEventResult =
        CotParser.parse(xml).fold(
            onSuccess = { JsCotEventResult(ok = true, value = it.toJsEvent()) },
            onFailure = { JsCotEventResult(ok = false, error = it.message ?: "Unknown parse error.") },
        )

    fun serialize(event: JsCotEvent): JsStringResult =
        CotSerializer.serialize(event.toInternalEvent()).fold(
            onSuccess = { JsStringResult(ok = true, value = it) },
            onFailure = { JsStringResult(ok = false, error = it.message ?: "Unknown serialization error.") },
        )

    fun cotTypeToSidc(cotType: String, standard: JsSidcStandard): JsStringResult =
        cotTypeToSidc(cotType, standard.toInternal()).fold(
            onSuccess = { JsStringResult(ok = true, value = it) },
            onFailure = { JsStringResult(ok = false, error = it.message ?: "Unknown SIDC conversion error.") },
        )

    fun sidcToCotType(sidc: String, standard: JsSidcStandard): JsStringResult =
        sidcToCotType(sidc, standard.toInternal()).fold(
            onSuccess = { JsStringResult(ok = true, value = it) },
            onFailure = { JsStringResult(ok = false, error = it.message ?: "Unknown SIDC reverse-conversion error.") },
        )
}

private fun JsSidcStandard.toInternal(): SidcStandard = when (this) {
    JsSidcStandard.MIL_STD_2525B -> SidcStandard.MIL_STD_2525B
    JsSidcStandard.MIL_STD_2525C -> SidcStandard.MIL_STD_2525C
    JsSidcStandard.MIL_STD_2525D -> SidcStandard.MIL_STD_2525D
}

private fun CotEvent.toJsEvent(): JsCotEvent = JsCotEvent(
    uid = uid,
    type = type,
    time = time,
    start = start,
    stale = stale,
    how = how,
    point = point.toJsPoint(),
    detail = detail?.toJsDetail(),
)

private fun CotPoint.toJsPoint(): JsCotPoint = JsCotPoint(
    lat = lat,
    lon = lon,
    hae = hae,
    ce = ce,
    le = le,
)

private fun CotDetail.toJsDetail(): JsCotDetail = JsCotDetail(
    callsign = callsign,
    phone = phone,
    remarks = remarks,
)

private fun JsCotEvent.toInternalEvent(): CotEvent = CotEvent(
    uid = uid,
    type = type,
    time = time,
    start = start,
    stale = stale,
    how = how,
    point = CotPoint(
        lat = point.lat,
        lon = point.lon,
        hae = point.hae,
        ce = point.ce,
        le = point.le,
    ),
    detail = detail?.let {
        CotDetail(
            callsign = it.callsign,
            phone = it.phone,
            remarks = it.remarks,
        )
    },
)
