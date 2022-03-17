package io.sunland.chainpass.common.security

import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

actual object PasswordEncoder {
    actual fun hash(passphrase: EncoderSpec.Passphrase): String {
        val keySpec = PBEKeySpec(
            passphrase.key.toCharArray(),
            MessageDigest.getInstance(EncoderSpec.SHA256).digest(passphrase.seed.encodeToByteArray()),
            EncoderSpec.Strength.ITERATION_COUNT,
            EncoderSpec.Strength.KEY_LENGTH
        )

        val secretKey = SecretKeyFactory.getInstance(EncoderSpec.Algorithm.PBKDF2WithHmacSHA256).generateSecret(keySpec)

        return Base64.getEncoder().encodeToString(secretKey.encoded)
    }

    actual fun encrypt(passphrase: EncoderSpec.Passphrase, password: String): String {
        val secretKey = SecretKeySpec(
            passphrase.key.encodeToByteArray().copyOf(EncoderSpec.Strength.KEY_LENGTH / 8),
            EncoderSpec.AES
        )

        val ivParamSpec = GCMParameterSpec(
            EncoderSpec.Strength.TAG_LENGTH,
            MessageDigest.getInstance(EncoderSpec.SHA256).digest(passphrase.seed.encodeToByteArray())
                .copyOf(EncoderSpec.Strength.IV_LENGTH / 8)
        )

        return Cipher.getInstance(EncoderSpec.Algorithm.AESGCMNoPadding).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParamSpec)
            Base64.getEncoder().encodeToString(cipher.doFinal(password.encodeToByteArray()))
        }
    }

    actual fun decrypt(passphrase: EncoderSpec.Passphrase, password: String): String {
        val secretKey = SecretKeySpec(
            passphrase.key.encodeToByteArray().copyOf(EncoderSpec.Strength.KEY_LENGTH / 8),
            EncoderSpec.AES
        )

        val ivParamSpec = GCMParameterSpec(
            EncoderSpec.Strength.TAG_LENGTH,
            MessageDigest.getInstance(EncoderSpec.SHA256).digest(passphrase.seed.encodeToByteArray())
                .copyOf(EncoderSpec.Strength.IV_LENGTH / 8)
        )

        return Cipher.getInstance(EncoderSpec.Algorithm.AESGCMNoPadding).let { cipher ->
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParamSpec)
            cipher.doFinal(Base64.getDecoder().decode(password.encodeToByteArray())).decodeToString()
        }
    }
}
