package io.github.oxiadenine.chainpass.security

import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

actual object PasswordEncoder {
    actual object Base64 {
        actual fun encode(text: ByteArray): String = java.util.Base64.getEncoder().encodeToString(text)
        actual fun decode(text: String): ByteArray = java.util.Base64.getDecoder().decode(text)
    }

    actual object Salt {
        actual fun generate() = Base64.encode(Random.nextBytes(EncoderSpec.Strength.SALT_LENGTH / 8))
    }

    actual object IV {
        actual fun generate() = Base64.encode(Random.nextBytes(EncoderSpec.Strength.IV_LENGTH / 8))
    }

    actual fun hash(password: String, salt: String): String {
        val keySpec = PBEKeySpec(
            Base64.decode(password).decodeToString().toCharArray(),
            Base64.decode(salt),
            EncoderSpec.Strength.ITERATION_COUNT,
            EncoderSpec.Strength.KEY_LENGTH
        )

        val secretKey = SecretKeyFactory.getInstance(EncoderSpec.Algorithm.PBKDF2_WITH_HMAC_SHA256).generateSecret(keySpec)

        return Base64.encode(secretKey.encoded)
    }

    actual fun encrypt(password: String, key: String, iv: String): String {
        val secretKey = SecretKeySpec(Base64.decode(key), EncoderSpec.Algorithm.AES)

        val ivParamSpec = GCMParameterSpec(EncoderSpec.Strength.TAG_LENGTH, Base64.decode(iv))

        return Cipher.getInstance(EncoderSpec.Algorithm.AES_GCM_NO_PADDING).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParamSpec)
            Base64.encode(cipher.doFinal(Base64.decode(password)))
        }
    }

    actual fun decrypt(password: String, key: String, iv: String): String {
        val secretKey = SecretKeySpec(Base64.decode(key), EncoderSpec.Algorithm.AES)

        val ivParamSpec = GCMParameterSpec(EncoderSpec.Strength.TAG_LENGTH, Base64.decode(iv))

        return Cipher.getInstance(EncoderSpec.Algorithm.AES_GCM_NO_PADDING).let { cipher ->
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParamSpec)
            Base64.encode(cipher.doFinal(Base64.decode(password)))
        }
    }
}