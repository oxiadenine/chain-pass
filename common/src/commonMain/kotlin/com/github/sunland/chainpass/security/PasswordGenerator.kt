package com.github.sunland.chainpass.security

class PasswordGenerator(private val strength: Strength) {
    enum class CharCodes(val ranges: Array<Pair<Int, Int>>) {
        ALPHANUMERIC(arrayOf(48 to 57, 65 to 90, 97 to 122)),
        SYMBOL(arrayOf(33 to 47, 58 to 64, 91 to 96, 123 to 126))
    }

    data class Strength(val length: Int = 16, val isAlphanumeric: Boolean = false)

    fun generate(): String {
        if (strength.length <= 0) {
            return ""
        }

        val charCodeNumbers = mutableListOf<Int>()

        var charCodesSymbolCount = 0

        repeat(strength.length) {
            val charCodeRange = if (charCodesSymbolCount == strength.length / 4 || strength.isAlphanumeric) {
                 CharCodes.ALPHANUMERIC.ranges.random()
            } else {
                charCodesSymbolCount += 1

                CharCodes.SYMBOL.ranges .random()
            }

            charCodeNumbers.add(Random.nextInt(charCodeRange))
        }

        return charCodeNumbers.map { charNumber -> charNumber.toChar() }.shuffled().toCharArray().concatToString()
    }
}