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

package cot

import cot.model.CotEvent
import cot.parser.CotParser
import cot.serializer.CotSerializer
import cot.sidc.SidcStandard

/**
 * Java-friendly entry point for the cot-kmp library.
 *
 * Each method delegates to the shared Kotlin implementation and unwraps [Result]
 * via [Result.getOrThrow], throwing [IllegalArgumentException] on failure.
 * Use this object from Java; Kotlin callers should use the underlying APIs directly.
 */
object CotJvmApi {

    /**
     * Parses a COT XML string into a [CotEvent].
     *
     * @throws IllegalArgumentException if the XML is blank, malformed, or missing required fields.
     */
    @JvmStatic
    fun parse(xml: String): CotEvent = CotParser.parse(xml).getOrThrow()

    /**
     * Serializes a [CotEvent] into a COT XML string.
     *
     * @throws IllegalArgumentException if serialization fails.
     */
    @JvmStatic
    fun serialize(event: CotEvent): String = CotSerializer.serialize(event).getOrThrow()

    /**
     * Converts a COT type string (e.g. "a-f-G-U-C") to a SIDC string using the given standard.
     *
     * @throws IllegalArgumentException if the COT type is malformed or contains unrecognized codes.
     */
    @JvmStatic
    fun cotTypeToSidc(cotType: String, standard: SidcStandard): String =
        cot.sidc.cotTypeToSidc(cotType, standard).getOrThrow()

    /**
     * Converts a SIDC string back to a COT type string using the given standard.
     *
     * @throws IllegalArgumentException if the SIDC is malformed or contains unrecognized codes.
     */
    @JvmStatic
    fun sidcToCotType(sidc: String, standard: SidcStandard): String =
        cot.sidc.sidcToCotType(sidc, standard).getOrThrow()
}
