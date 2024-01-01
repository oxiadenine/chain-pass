package io.sunland.chainpass.common.security

object EncoderSpec {
    const val AES = "AES"
    const val SHA256 = "SHA-256"

    object Algorithm {
        const val AESGCMNoPadding = "AES/GCM/NoPadding"
        const val PBKDF2WithHmacSHA256 = "PBKDF2WithHmacSHA256"
    }

    object Strength {
        const val ITERATION_COUNT = 100000
        const val KEY_LENGTH = 256
        const val TAG_LENGTH = 128
    }

    data class Passphrase(val key: String, val salt: String)
}

expect object PasswordEncoder {
    object Base64 {
        fun encode(text: ByteArray): String
        fun decode(text: String): ByteArray
    }

    fun hash(passphrase: EncoderSpec.Passphrase): String

    fun encrypt(password: String, passphrase: EncoderSpec.Passphrase): String
    fun decrypt(password: String, passphrase: EncoderSpec.Passphrase): String
}
