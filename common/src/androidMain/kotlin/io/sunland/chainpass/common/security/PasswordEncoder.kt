package io.sunland.chainpass.common.security

import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

actual object PasswordEncoder {
    actual fun encode(password: String, seed: String): String {
        val digest = MessageDigest.getInstance(Encoder.Algorithm.SALT)
        val salt = digest.digest(seed.toByteArray())

        val spec = PBEKeySpec(password.toCharArray(), salt, Encoder.Strength.ITERATION_COUNT, Encoder.Strength.KEY_LENGTH)

        return SecretKeyFactory.getInstance(Encoder.Algorithm.KEY).generateSecret(spec).encoded
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
