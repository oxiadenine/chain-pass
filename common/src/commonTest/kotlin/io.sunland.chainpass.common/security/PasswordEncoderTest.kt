package io.sunland.chainpass.common.security

import io.sunland.chainpass.common.AndroidIgnore
import kotlin.test.Test
import kotlin.test.assertEquals

@AndroidIgnore
class PasswordEncoderTest {
    private val hash = "o9N4Ue1+qSP7ztU40OB69iHJCpNDU+iX9bQIMRdEkYk="

    @Test
    fun hashTest() {
        assertEquals(hash, PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode("test".encodeToByteArray()),
            PasswordEncoder.Base64.encode("test".encodeToByteArray())
        )))
    }

    @Test
    fun encryptDecryptTest() {
        val encryptedPassword = PasswordEncoder.encrypt(
            EncoderSpec.Passphrase(hash, hash),
            PasswordEncoder.Base64.encode("test".encodeToByteArray())
        )

        val plainPassword = PasswordEncoder.decrypt(EncoderSpec.Passphrase(hash, hash), encryptedPassword)

        assertEquals("test", PasswordEncoder.Base64.decode(plainPassword).decodeToString())
    }
}
