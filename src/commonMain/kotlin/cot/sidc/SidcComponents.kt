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

package cot.sidc

/**
 * Supported SIDC standards.
 */
enum class SidcStandard {
    /** MIL-STD-2525B (2005), 15-character SIDC. AIR no-function uses "------"; "AA" is a real function ID. */
    MIL_STD_2525B,

    /** MIL-STD-2525C, represented here with 15-character fixtures. */
    MIL_STD_2525C,

    /** MIL-STD-2525D, represented here using the prompt's required fixtures. */
    MIL_STD_2525D,
}

/**
 * Unit or track affiliation shared between COT and SIDC forms.
 *
 * @property cotCode Affiliation code in COT type strings.
 * @property sidc2525C Affiliation code used in 2525C.
 * @property sidc2525D Standard identity code used in 2525D fixtures.
 */
enum class Affiliation(
    val cotCode: String,
    val sidc2525C: String,
    val sidc2525D: String,
) {
    UNKNOWN("u", "U", "1"),
    FRIENDLY("f", "F", "3"),
    HOSTILE("h", "H", "6"),
    NEUTRAL("n", "N", "4"),
    ASSUMED_FRIENDLY("a", "A", "2"),
    SUSPECT("s", "S", "5"),
    JOKER("j", "J", "5"),
    FAKER("k", "K", "5"),
    ;

    companion object {
        /**
         * Finds an affiliation by COT code.
         */
        fun fromCotCode(code: String): Affiliation? = entries.firstOrNull { it.cotCode == code }

        /**
         * Finds an affiliation by 2525C SIDC code.
         */
        fun fromSidc2525C(code: String): Affiliation? = entries.firstOrNull { it.sidc2525C == code }

        /**
         * Finds an affiliation by the 2525D fixture identity code.
         */
        fun fromSidc2525D(code: String): Affiliation? = entries.firstOrNull { it.sidc2525D == code }
    }
}

/**
 * Battle dimension or operating domain shared between COT and SIDC forms.
 *
 * @property cotCode Dimension code in COT type strings.
 * @property sidc2525C Dimension code used in 2525C.
 * @property symbolSet Symbol set used in 2525D fixtures.
 */
enum class BattleDimension(
    val cotCode: String,
    val sidc2525C: String,
    val symbolSet: String,
) {
    SPACE("P", "P", "05"),
    AIR("A", "A", "10"),
    GROUND("G", "G", "10"),
    SEA_SURFACE("S", "S", "30"),
    SUBSURFACE("U", "U", "35"),
    SOF("F", "F", "27"),
    OTHER("X", "X", "10"),
    ;

    companion object {
        /**
         * Finds a dimension by COT code.
         */
        fun fromCotCode(code: String): BattleDimension? = entries.firstOrNull { it.cotCode == code }

        /**
         * Finds a dimension by 2525C SIDC code.
         */
        fun fromSidc2525C(code: String): BattleDimension? = entries.firstOrNull { it.sidc2525C == code }

        /**
         * Finds a dimension by 2525D symbol set.
         */
        fun fromSymbolSet(code: String): BattleDimension? = entries.firstOrNull { it.symbolSet == code }
    }
}
