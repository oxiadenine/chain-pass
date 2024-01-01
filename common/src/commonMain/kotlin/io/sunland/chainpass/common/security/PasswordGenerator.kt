package io.sunland.chainpass.common.security

class PasswordGenerator(private val strength: Strength) {
    enum class CharCodeRanges(val ranges: Array<Pair<Int, Int>>) {
        ALPHABET(arrayOf(65 to 90, 97 to 122)),
        NUMERIC(arrayOf(48 to 57)),
        SYMBOL(arrayOf(33 to 47, 58 to 64, 91 to 96, 123 to 126))
    }

    data class Strength(val length: Int = 16, val isAlphanumeric: Boolean = false)

    fun generate(): String {
        if (strength.length <= 0) {
            return ""
        }

        val charNumbers = mutableListOf<Int>()

        var charCount = strength.length

        while (charCount > 0) {
            charCount /= 2

            val charCodeRanges = if (charCount > (strength.length / 2) - (charCount * 2)) {
                if (charCount > (strength.length / 2) - (charCount / 2) || strength.isAlphanumeric) {
                    CharCodeRanges.ALPHABET
                } else {
                    CharCodeRanges.SYMBOL
                }
            } else {
                if (charCount < 2 || strength.isAlphanumeric) {
                    CharCodeRanges.NUMERIC
                } else {
                    CharCodeRanges.SYMBOL
                }
            }

            repeat(if (charCount % 2 == 0) charCount else charCount + 1) { count ->
                val charCodeRange = when (charCodeRanges) {
                    CharCodeRanges.ALPHABET -> charCodeRanges.ranges[count % 2]
                    CharCodeRanges.NUMERIC -> charCodeRanges.ranges[0]
                    CharCodeRanges.SYMBOL -> charCodeRanges.ranges[count % 2]
                }

                charNumbers.add(Random.nextInt(charCodeRange))
            }

            if (strength.length % 2 != 0 && charCount == 0) {
                charNumbers.add(Random.nextInt(charCodeRanges.ranges[0]))
            }
        }

        return charNumbers.map { charNumber -> charNumber.toChar() }.shuffled().toCharArray().concatToString()
    }
}