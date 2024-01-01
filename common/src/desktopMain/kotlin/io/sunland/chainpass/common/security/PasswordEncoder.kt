package io.sunland.chainpass.common.security

import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

actual object PasswordEncoder {
    actual fun hash(password: String, seed: String): String {
        val digest = MessageDigest.getInstance(EncoderSpec.Algorithm.SALT)
        val salt = digest.digest(seed.toByteArray())

        val spec = PBEKeySpec(password.toCharArray(), salt, EncoderSpec.Strength.ITERATION_COUNT, EncoderSpec.Strength.KEY_LENGTH)

        return SecretKeyFactory.getInstance(EncoderSpec.Algorithm.KEY).generateSecret(spec).encoded
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    actual fun encrypt(key: String, seed: String, password: String): String {
        val secretKey = SecretKeySpec(key.encodeToByteArray().copyOf(32), EncoderSpec.Algorithm.SECRET_KEY)

        val digest = MessageDigest.getInstance(EncoderSpec.Algorithm.IV)
        val ivParamSpec = IvParameterSpec(digest.digest(seed.encodeToByteArray()).copyOf(16))

        return Cipher.getInstance(EncoderSpec.Algorithm.PASSWORD).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParamSpec)
            Base64.getEncoder().encodeToString(cipher.doFinal(password.encodeToByteArray()))
        }
    }

    actual fun decrypt(key: String, seed: String, password: String): String {
        val secretKey = SecretKeySpec(key.encodeToByteArray().copyOf(32), EncoderSpec.Algorithm.SECRET_KEY)

        val digest = MessageDigest.getInstance(EncoderSpec.Algorithm.IV)
        val ivParamSpec = IvParameterSpec(digest.digest(seed.encodeToByteArray()).copyOf(16))

        return Cipher.getInstance(EncoderSpec.Algorithm.PASSWORD).let { cipher ->
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParamSpec)
            cipher.doFinal(Base64.getDecoder().decode(password.encodeToByteArray())).decodeToString()
        }
    }
}
