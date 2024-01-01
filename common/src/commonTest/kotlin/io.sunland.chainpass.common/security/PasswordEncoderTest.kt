package io.sunland.chainpass.common.security

import io.sunland.chainpass.common.AndroidIgnore
import kotlin.test.Test
import kotlin.test.assertEquals

@AndroidIgnore
class PasswordEncoderTest {
    private val hash = "yOWTvJt/djLbFr+Caj/7SieMbQp0xR+cskIeNHzwsZQ="

    @Test
    fun testHash() {
        assertEquals(hash, PasswordEncoder.hash(EncoderSpec.Passphrase(
            PasswordEncoder.Base64.encode("test".encodeToByteArray()),
            PasswordEncoder.Base64.encode("test".encodeToByteArray())
        )))
    }

    @Test
    fun testEncryptDecrypt() {
        val encryptedPassword = PasswordEncoder.encrypt(
            PasswordEncoder.Base64.encode("test".encodeToByteArray()),
            EncoderSpec.Passphrase(hash, PasswordEncoder.Base64.encode("test".encodeToByteArray()))
        )

        val plainPassword = PasswordEncoder.decrypt(
            encryptedPassword,
            EncoderSpec.Passphrase(hash, PasswordEncoder.Base64.encode("test".encodeToByteArray()))
        )

        assertEquals("test", PasswordEncoder.Base64.decode(plainPassword).decodeToString())
    }
}
