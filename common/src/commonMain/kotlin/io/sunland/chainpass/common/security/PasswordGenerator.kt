package io.sunland.chainpass.common.security

expect object RandomInt {
    fun next(): Int
    fun next(range: Pair<Int, Int>): Int

    fun nextArray(size: Int, range: Pair<Int, Int>): Array<Int>
}

object GeneratorSpec {
    enum class CharCodeRanges(val ranges: Array<Pair<Int, Int>>) {
        ALPHABET(arrayOf(65 to 90, 97 to 122)),
        NUMERIC(arrayOf(48 to 57)),
        SYMBOL(arrayOf(33 to 47, 58 to 64, 91 to 96, 123 to 126))
    }

    data class Strength(val length: Int, val alphanumeric: Boolean = false)
}

class PasswordGenerator(private val strength: GeneratorSpec.Strength) {
    fun generate(length: Int = strength.length, alphanumeric: Boolean = strength.alphanumeric): String {
        if (length <= 0) {
            return ""
        }

        val charNumbers = mutableListOf<Int>()

        var charCount = length

        while (charCount > 0) {
            charCount /= 2

            val charCodeRanges = if (charCount > (length / 2) - (charCount * 2)) {
                if (charCount > (length / 2) - (charCount / 2) || alphanumeric) {
                    GeneratorSpec.CharCodeRanges.ALPHABET
                } else {
                    GeneratorSpec.CharCodeRanges.SYMBOL
                }
            } else {
                if (charCount < 2 || alphanumeric) {
                    GeneratorSpec.CharCodeRanges.NUMERIC
                } else {
                    GeneratorSpec.CharCodeRanges.SYMBOL
                }
            }

            repeat(if (charCount % 2 == 0) charCount else charCount + 1) { count ->
                val charCodeRange = when (charCodeRanges) {
                    GeneratorSpec.CharCodeRanges.ALPHABET -> charCodeRanges.ranges[count % 2]
                    GeneratorSpec.CharCodeRanges.NUMERIC -> charCodeRanges.ranges[0]
                    GeneratorSpec.CharCodeRanges.SYMBOL -> charCodeRanges.ranges[count % 2]
                }

                charNumbers.add(RandomInt.next(charCodeRange))
            }

            if (length % 2 != 0 && charCount == 0) {
                charNumbers.add(RandomInt.next(charCodeRanges.ranges[0]))
            }
        }

        return charNumbers.map { charNumber -> charNumber.toChar() }.shuffled().toCharArray().concatToString()
    }
}