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
 * Converts a SIDC string back into a COT type string.
 */
fun sidcToCotType(sidc: String, standard: SidcStandard? = null): Result<String> = runCatching {
    require(sidc.isNotBlank()) {
        "SIDC string must not be blank."
    }

    when (standard ?: detectStandard(sidc)) {
        SidcStandard.MIL_STD_2525B -> decode2525B(sidc)
        SidcStandard.MIL_STD_2525C -> decode2525C(sidc)
        SidcStandard.MIL_STD_2525D -> decode2525D(sidc)
    }
}

private fun detectStandard(sidc: String): SidcStandard = when (sidc.length) {
    15 -> SidcStandard.MIL_STD_2525C
    18, 19, 20 -> SidcStandard.MIL_STD_2525D
    else -> throw IllegalArgumentException(
        "Unsupported SIDC length ${sidc.length} for '$sidc'. Expected 15, 18, 19, or 20 characters."
    )
}

private fun decode2525C(sidc: String): String {
    require(sidc.length == 15) {
        "MIL-STD-2525C SIDC must be 15 characters. Got '${sidc.length}' from '$sidc'."
    }

    val affiliation = requireNotNull(Affiliation.fromSidc2525C(sidc[1].toString())) {
        "Unrecognized 2525C affiliation '${sidc[1]}' in SIDC '$sidc'."
    }
    val dimension = requireNotNull(BattleDimension.fromSidc2525C(sidc[2].toString())) {
        "Unrecognized 2525C battle dimension '${sidc[2]}' in SIDC '$sidc'."
    }

    val functionId = sidc.substring(4, 10)
    if (dimension == BattleDimension.AIR && functionId == "AA----") {
        return buildCotTypeString(affiliation, dimension, null, null)
    }

    val function = functionId.getOrNull(0)?.takeIf { it != '-' }?.toString()
    val modifier = functionId.getOrNull(1)?.takeIf { it != '-' }?.toString()
    return buildCotTypeString(affiliation, dimension, function, modifier)
}

private fun decode2525B(sidc: String): String {
    require(sidc.length == 15) {
        "MIL-STD-2525B SIDC must be 15 characters. Got '${sidc.length}' from '$sidc'."
    }

    val affiliation = requireNotNull(Affiliation.fromSidc2525C(sidc[1].toString())) {
        "Unrecognized 2525B affiliation '${sidc[1]}' in SIDC '$sidc'."
    }
    val dimension = requireNotNull(BattleDimension.fromSidc2525C(sidc[2].toString())) {
        "Unrecognized 2525B battle dimension '${sidc[2]}' in SIDC '$sidc'."
    }

    // No AA---- special case here — in 2525B, AA is a real function ID (fixed-wing aircraft).
    // "SFAPAA---------" decodes as function='A', modifier='A' → COT type "a-f-A-A-A".
    val functionId = sidc.substring(4, 10)
    val function = functionId.getOrNull(0)?.takeIf { it != '-' }?.toString()
    val modifier = functionId.getOrNull(1)?.takeIf { it != '-' }?.toString()
    return buildCotTypeString(affiliation, dimension, function, modifier)
}

private fun decode2525D(sidc: String): String {
    require(sidc.length == 18 || sidc.length == 19 || sidc.length == 20) {
        "MIL-STD-2525D SIDC must be 18, 19, or 20 characters for this library. " +
            "Got '${sidc.length}' from '$sidc'."
    }

    val affiliation = requireNotNull(Affiliation.fromSidc2525D(sidc[3].toString())) {
        "Unrecognized 2525D affiliation '${sidc[3]}' in SIDC '$sidc'."
    }
    val symbolSet = sidc.substring(4, 6)
    val entity = sidc.substring(9)
    val function = entity.getOrNull(0)?.takeIf { it != '0' }?.toString()
    val modifier = entity.getOrNull(1)?.takeIf { it != '0' }?.toString()
    val dimension = resolve2525DDimension(symbolSet, affiliation, function, modifier, sidc)
    return buildCotTypeString(affiliation, dimension, function, modifier)
}

private fun resolve2525DDimension(
    symbolSet: String,
    affiliation: Affiliation,
    function: String?,
    modifier: String?,
    sidc: String,
): BattleDimension {
    if (symbolSet != "10") {
        return requireNotNull(BattleDimension.fromSymbolSet(symbolSet)) {
            "Unrecognized 2525D symbol set '$symbolSet' in SIDC '$sidc'."
        }
    }

    return when {
        function != null || modifier != null -> BattleDimension.GROUND
        affiliation == Affiliation.HOSTILE -> BattleDimension.AIR
        else -> BattleDimension.GROUND
    }
}

private fun buildCotTypeString(
    affiliation: Affiliation,
    dimension: BattleDimension,
    function: String?,
    modifier: String?,
): String = buildString {
    append("a")
    append("-")
    append(affiliation.cotCode)
    append("-")
    append(dimension.cotCode)
    function?.let {
        append("-")
        append(it)
    }
    modifier?.let {
        append("-")
        append(it)
    }
}
