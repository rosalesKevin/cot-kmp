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
 * Represents a single child element inside the COT `<detail>` block.
 *
 * Each element is captured structurally — tag name, attributes, optional text content,
 * and any nested child elements — so consumers can inspect or manipulate vendor
 * extensions (e.g. ATAK's `<__group>`, `<takv>`, `<status>`) without parsing raw XML.
 *
 * @property tag Local element name (e.g. `"__group"`, `"takv"`).
 * @property attributes Attribute map for this element; keys and values are already
 *   XML-unescaped by the parser.
 * @property text Text content of this element if present, already XML-unescaped.
 * @property children Nested child elements, preserving document order.
 */
@Serializable
data class DetailElement(
    val tag: String,
    val attributes: Map<String, String> = emptyMap(),
    val text: String? = null,
    val children: List<DetailElement> = emptyList(),
)
