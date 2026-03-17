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

package cot.sidc

/**
 * Parsed representation of a COT type string.
 *
 * @property scheme COT scheme segment.
 * @property affiliation Affiliation segment.
 * @property dimension Battle dimension segment.
 * @property function Optional function segment.
 * @property modifier Optional modifier segment.
 * @property raw Original COT type string.
 */
data class CotType(
    val scheme: String,
    val affiliation: String,
    val dimension: String,
    val function: String? = null,
    val modifier: String? = null,
    val raw: String,
) {
    companion object {
        /**
         * Parses a raw COT type string.
         */
        fun parse(cotTypeString: String): Result<CotType> = runCatching {
            require(cotTypeString.isNotBlank()) {
                "COT type string must not be blank."
            }

            val parts = cotTypeString.split("-")
            require(parts.size in 3..5) {
                "COT type string must have 3 to 5 dash-separated segments. " +
                    "Got '$cotTypeString' with ${parts.size} segments."
            }

            CotType(
                scheme = parts[0],
                affiliation = parts[1],
                dimension = parts[2],
                function = parts.getOrNull(3),
                modifier = parts.getOrNull(4),
                raw = cotTypeString,
            )
        }
    }

    /**
     * Reconstructs the COT type string from parsed segments.
     */
    fun toTypeString(): String = buildString {
        append(scheme)
        append("-")
        append(affiliation)
        append("-")
        append(dimension)
        function?.let {
            append("-")
            append(it)
        }
        modifier?.let {
            append("-")
            append(it)
        }
    }
}
