package io.sunland.chainpass.common.security

object EncoderSpec {
    object Algorithm {
        const val KEY = "PBKDF2WithHmacSHA512"
        const val SECRET_KEY = "AES"
        const val PASSWORD = "AES/CBC/PKCS5Padding"
        const val SALT = "SHA-512"
        const val IV = "SHA-256"
    }

    object Strength {
        const val ITERATION_COUNT = 100000
        const val KEY_LENGTH = 256
    }
}

expect object PasswordEncoder {
    fun hash(password: String, seed: String): String

    fun encrypt(key: String, seed: String, password: String): String
    fun decrypt(key: String, seed: String, password: String): String
}
