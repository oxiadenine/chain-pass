package io.sunland.chainpass.common.security

sealed class Encoder {
    object Algorithm {
        const val SALT = "SHA-512"
        const val KEY = "PBKDF2WithHmacSHA512"
    }

    object Strength {
        const val ITERATION_COUNT = 100000
        const val KEY_LENGTH = 256
    }
}

expect object PasswordEncoder {
    fun encode(password: String, seed: String): String
}
