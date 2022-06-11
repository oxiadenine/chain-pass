package io.sunland.chainpass.common.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PasswordGeneratorTest {
    private val passwordGenerator = PasswordGenerator(GeneratorSpec.Strength(16))

    @Test
    fun testGenerate() {
        assertEquals(16, passwordGenerator.generate().length)
        assertEquals(1, passwordGenerator.generate(1).length)
        assertEquals(5, passwordGenerator.generate(5).length)
        assertEquals(8, passwordGenerator.generate(8).length)

        assertTrue(passwordGenerator.generate().matches("^[^\\s]{16}$".toRegex()))
        assertTrue(passwordGenerator.generate(1).matches("^[^\\s]$".toRegex()))
        assertTrue(passwordGenerator.generate(5).matches("^[^\\s]{5}$".toRegex()))
        assertTrue(passwordGenerator.generate(8).matches("^[^\\s]{8}$".toRegex()))

        assertTrue(passwordGenerator.generate(alphanumeric = true).matches("^[a-zA-Z\\d]{16}$".toRegex()))
        assertTrue(passwordGenerator.generate(1, true).matches("^[a-zA-Z\\d]$".toRegex()))
        assertTrue(passwordGenerator.generate(5, true).matches("^[a-zA-Z\\d]{5}$".toRegex()))
        assertTrue(passwordGenerator.generate(8, true).matches("^[a-zA-Z\\d]{8}$".toRegex()))

        assertTrue(passwordGenerator.generate(0) == "")
        assertTrue(passwordGenerator.generate(-1) == "")
    }
}