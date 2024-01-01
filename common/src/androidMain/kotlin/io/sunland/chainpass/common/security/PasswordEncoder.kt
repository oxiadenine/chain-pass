package io.sunland.chainpass.common.security

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

actual object PasswordEncoder {
    actual fun hash(passphrase: EncoderSpec.Passphrase): String {
        val digest = MessageDigest.getInstance(EncoderSpec.Algorithm.SHA256)
        val salt = digest.digest(passphrase.seed.encodeToByteArray())

        val keySpec = PBEKeySpec(
            passphrase.key.toCharArray(),
            salt,
            EncoderSpec.Strength.ITERATION_COUNT,
            EncoderSpec.Strength.KEY_LENGTH
        )
        val secretKey = SecretKeyFactory.getInstance(EncoderSpec.Algorithm.PBKDF2WithHmacSHA256).generateSecret(keySpec)

        return Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
    }

    actual fun encrypt(passphrase: EncoderSpec.Passphrase, password: String): String {
        val secretKey = SecretKeySpec(passphrase.key.encodeToByteArray().copyOf(32), EncoderSpec.Algorithm.AES)

        val digest = MessageDigest.getInstance(EncoderSpec.Algorithm.SHA256)
        val ivParamSpec = IvParameterSpec(digest.digest(passphrase.seed.encodeToByteArray()).copyOf(16))

        return Cipher.getInstance(EncoderSpec.Algorithm.AESCBCPKCS5Padding).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParamSpec)
            Base64.encodeToString(cipher.doFinal(password.encodeToByteArray()), Base64.NO_WRAP)
        }
    }

    actual fun decrypt(passphrase: EncoderSpec.Passphrase, password: String): String {
        val secretKey = SecretKeySpec(passphrase.key.encodeToByteArray().copyOf(32), EncoderSpec.Algorithm.AES)

        val digest = MessageDigest.getInstance(EncoderSpec.Algorithm.SHA256)
        val ivParamSpec = IvParameterSpec(digest.digest(passphrase.seed.encodeToByteArray()).copyOf(16))

        return Cipher.getInstance(EncoderSpec.Algorithm.AESCBCPKCS5Padding).let { cipher ->
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParamSpec)
            cipher.doFinal(Base64.decode(password.encodeToByteArray(), Base64.NO_WRAP)).decodeToString()
        }
    }
}
