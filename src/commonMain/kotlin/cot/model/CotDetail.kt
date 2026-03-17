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

package cot.model

import kotlinx.serialization.Serializable

/**
 * Represents the optional `<detail>` block of a COT event.
 *
 * Known children (`<contact>` and `<remarks>`) are promoted to first-class fields for
 * convenience. All other children — including vendor-specific ATAK extensions such as
 * `<__group>`, `<takv>`, and `<status>` — are preserved structurally in [children] so
 * consumers can inspect or extend them without re-parsing XML.
 *
 * @property callsign Human-readable contact identifier (from `<contact callsign="...">`).
 * @property phone Contact phone number (from `<contact phone="...">`).
 * @property remarks Free-form remarks (from `<remarks>…</remarks>`).
 * @property children Structured representation of all other `<detail>` child elements,
 *   in document order. Replaces the previous `rawXml` string blob.
 */
@Serializable
data class CotDetail(
    val callsign: String? = null,
    val phone: String? = null,
    val remarks: String? = null,
    val children: List<DetailElement> = emptyList(),
)
