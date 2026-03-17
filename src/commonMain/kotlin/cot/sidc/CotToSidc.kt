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
 * Converts a COT type string to a SIDC string using the requested standard.
 */
fun cotTypeToSidc(cotTypeString: String, standard: SidcStandard): Result<String> =
    CotType.parse(cotTypeString).mapCatching { cotType ->
        val affiliation = requireNotNull(Affiliation.fromCotCode(cotType.affiliation)) {
            "Unrecognized affiliation code '${cotType.affiliation}' in COT type '${cotType.raw}'. " +
                "Valid codes: ${Affiliation.entries.map { it.cotCode }}"
        }
        val dimension = requireNotNull(BattleDimension.fromCotCode(cotType.dimension)) {
            "Unrecognized battle dimension '${cotType.dimension}' in COT type '${cotType.raw}'. " +
                "Valid codes: ${BattleDimension.entries.map { it.cotCode }}"
        }

        when (standard) {
            SidcStandard.MIL_STD_2525B -> encode2525B(cotType, affiliation, dimension)
            SidcStandard.MIL_STD_2525C -> encode2525C(cotType, affiliation, dimension)
            SidcStandard.MIL_STD_2525D -> encode2525D(cotType, affiliation, dimension)
        }
    }

private fun encode2525C(
    cotType: CotType,
    affiliation: Affiliation,
    dimension: BattleDimension,
): String {
    val functionId = when {
        cotType.function == null && cotType.modifier == null && dimension == BattleDimension.AIR -> "AA----"
        cotType.function == null && cotType.modifier == null -> "------"
        else -> buildString {
            append(cotType.function ?: "-")
            append(cotType.modifier ?: "-")
            while (length < 6) {
                append("-")
            }
        }.take(6)
    }

    return buildString {
        append("S")
        append(affiliation.sidc2525C)
        append(dimension.sidc2525C)
        append("P")
        append(functionId)
        append("-----")
    }
}

private fun encode2525B(
    cotType: CotType,
    affiliation: Affiliation,
    dimension: BattleDimension,
): String {
    val functionId = when {
        // In 2525B, no-function/no-modifier is "------" for ALL dimensions including AIR.
        // 2525C differs here: it uses "AA----" for AIR to signal "no function".
        cotType.function == null && cotType.modifier == null -> "------"
        else -> buildString {
            append(cotType.function ?: "-")
            append(cotType.modifier ?: "-")
            while (length < 6) {
                append("-")
            }
        }.take(6)
    }

    return buildString {
        append("S")
        append(affiliation.sidc2525C)   // same letter codes as 2525C
        append(dimension.sidc2525C)
        append("P")
        append(functionId)
        append("-----")
    }
}

private fun encode2525D(
    cotType: CotType,
    affiliation: Affiliation,
    dimension: BattleDimension,
): String {
    val entityCode = if (cotType.function == null && cotType.modifier == null) {
        "000000000"
    } else {
        buildString {
            append(cotType.function ?: "0")
            append(cotType.modifier ?: "0")
            repeat(8) { append("0") }
        }
    }

    return buildString {
        append("10")
        append("0")
        append(affiliation.sidc2525D)
        append(dimension.symbolSet)
        append("0")
        append("00")
        append(entityCode)
    }
}
