package io.sunland.chainpass.common.security

object EncoderSpec {
    object Algorithm {
        const val AES = "AES"
        const val SHA256 = "SHA-256"
        const val AESCBCPKCS5Padding = "AES/CBC/PKCS5Padding"
        const val PBKDF2WithHmacSHA256 = "PBKDF2WithHmacSHA256"
    }

    object Strength {
        const val ITERATION_COUNT = 100000
        const val KEY_LENGTH = 256
    }

    data class Passphrase(val key: String, val seed: String)
}

expect object PasswordEncoder {
    fun hash(passphrase: EncoderSpec.Passphrase): String

    fun encrypt(passphrase: EncoderSpec.Passphrase, password: String): String
    fun decrypt(passphrase: EncoderSpec.Passphrase, password: String): String
}
