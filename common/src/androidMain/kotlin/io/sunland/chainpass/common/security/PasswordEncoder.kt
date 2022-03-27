package io.sunland.chainpass.common.security

import android.util.Base64.NO_WRAP
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

actual object PasswordEncoder {
    actual object Base64 {
        actual fun encode(text: ByteArray): String = android.util.Base64.encodeToString(text, NO_WRAP)
        actual fun decode(text: String): ByteArray = android.util.Base64.decode(text, NO_WRAP)
    }

    actual fun hash(passphrase: EncoderSpec.Passphrase): String {
        val keySpec = PBEKeySpec(
            passphrase.key.toCharArray(),
            MessageDigest.getInstance(EncoderSpec.SHA256).digest(Base64.decode(passphrase.salt)),
            EncoderSpec.Strength.ITERATION_COUNT,
            EncoderSpec.Strength.KEY_LENGTH
        )

        val secretKey = SecretKeyFactory.getInstance(EncoderSpec.Algorithm.PBKDF2WithHmacSHA256).generateSecret(keySpec)

        return Base64.encode(secretKey.encoded)
    }

    actual fun encrypt(passphrase: EncoderSpec.Passphrase, password: String): String {
        val secretKey = SecretKeySpec(Base64.decode(passphrase.key), EncoderSpec.AES)

        val ivParamSpec = GCMParameterSpec(
            EncoderSpec.Strength.TAG_LENGTH,
            MessageDigest.getInstance(EncoderSpec.SHA256).digest(Base64.decode(passphrase.salt))
        )

        return Cipher.getInstance(EncoderSpec.Algorithm.AESGCMNoPadding).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParamSpec)
            Base64.encode(cipher.doFinal(Base64.decode(password)))
        }
    }

    actual fun decrypt(passphrase: EncoderSpec.Passphrase, password: String): String {
        val secretKey = SecretKeySpec(Base64.decode(passphrase.key), EncoderSpec.AES)

        val ivParamSpec = GCMParameterSpec(
            EncoderSpec.Strength.TAG_LENGTH,
            MessageDigest.getInstance(EncoderSpec.SHA256).digest(Base64.decode(passphrase.salt))
        )

        return Cipher.getInstance(EncoderSpec.Algorithm.AESGCMNoPadding).let { cipher ->
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParamSpec)
            Base64.encode(cipher.doFinal(Base64.decode(password)))
        }
    }
}
