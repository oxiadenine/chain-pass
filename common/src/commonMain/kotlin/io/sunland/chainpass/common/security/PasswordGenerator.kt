package io.sunland.chainpass.common.security

import kotlin.random.Random

object GeneratorSpec {
    enum class CharacterCodes(val ranges: Array<IntRange>) {
        ALPHABET(arrayOf(65..90, 97..122)),
        NUMERIC(arrayOf(48..57)),
        SPECIAL(arrayOf(33..47, 58..64, 91..96, 123..126))
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

            val characterCodes = if (charCount > (length / 2) - (charCount * 2)) {
                if (charCount > (length / 2) - (charCount / 2) || alphanumeric) {
                    GeneratorSpec.CharacterCodes.ALPHABET // Appears more times
                } else {
                    GeneratorSpec.CharacterCodes.SPECIAL // Appears sometimes
                }
            } else {
                if (charCount < 2 || alphanumeric) {
                    GeneratorSpec.CharacterCodes.NUMERIC // Appears fewer times
                } else {
                    GeneratorSpec.CharacterCodes.SPECIAL // Appears sometimes
                }
            }

            repeat(if (charCount % 2 == 0) charCount else charCount + 1) { count ->
                val charRange = when (characterCodes) {
                    GeneratorSpec.CharacterCodes.ALPHABET -> characterCodes.ranges[count % 2]
                    GeneratorSpec.CharacterCodes.NUMERIC -> characterCodes.ranges[0]
                    GeneratorSpec.CharacterCodes.SPECIAL -> characterCodes.ranges[count % 2]
                }

                charNumbers.add(Random.nextInt(charRange.first, charRange.last))
            }

            if (length % 2 != 0 && charCount == 0) {
                val charRange = characterCodes.ranges[0]

                charNumbers.add(Random.nextInt(charRange.first, charRange.last))
            }
        }

        return charNumbers.map { charNumber -> charNumber.toChar() }.shuffled().toCharArray().concatToString()
    }
}