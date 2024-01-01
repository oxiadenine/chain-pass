package io.github.oxiadenine.chainpass.security

object EncoderSpec {
    object Algorithm {
        const val AES = "AES"
        const val AES_GCM_NO_PADDING = "AES/GCM/NoPadding"
        const val PBKDF2_WITH_HMAC_SHA256 = "PBKDF2WithHmacSHA256"
    }

    object Strength {
        const val ITERATION_COUNT = 100000
        const val KEY_LENGTH = 256
        const val SALT_LENGTH = 128
        const val IV_LENGTH = 96
        const val TAG_LENGTH = 128
    }
}

expect object PasswordEncoder {
    object Base64 {
        fun encode(text: ByteArray): String
        fun decode(text: String): ByteArray
    }

    object Salt {
        fun generate(): String
    }

    object IV {
        fun generate(): String
    }

    fun hash(password: String, salt: String): String

    fun encrypt(password: String, key: String, iv: String): String
    fun decrypt(password: String, key: String, iv: String): String
}