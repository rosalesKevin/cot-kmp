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

package cot.util

private val utcTimestampPattern =
    Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{3})?Z""")

/**
 * Utilities for validating COT UTC timestamps.
 */
object CotTimeUtil {

    /**
     * Returns true when [value] matches the expected COT UTC timestamp format.
     */
    fun isUtcTimestamp(value: String): Boolean = utcTimestampPattern.matches(value)

    /**
     * Returns [value] if it is a valid COT UTC timestamp.
     *
     * @throws IllegalArgumentException when the value does not match the expected format.
     */
    fun requireUtcTimestamp(value: String, fieldName: String): String {
        require(isUtcTimestamp(value)) {
            "Invalid UTC timestamp for '$fieldName'. Expected ISO-8601 UTC like " +
                "'2024-01-01T00:00:00.000Z', got '$value'."
        }
        return value
    }
}
