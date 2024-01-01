package io.sunland.chainpass.common

import io.sunland.chainpass.common.security.EncoderSpec
import io.sunland.chainpass.common.security.PasswordEncoder
import io.sunland.chainpass.common.security.Random

class Chain() {
    object KeyInvalidError : Error()

    constructor(chain: Chain) : this() {
        id = chain.id
        name = chain.name
        key = chain.key
    }

    class Name(value: String? = null) {
        object EmptyError : Error()
        object LengthError : Error()
        object InvalidError : Error()

        var value = value ?: ""
            private set

        val validation = value?.let {
            if (value.isEmpty()) {
                Result.failure(EmptyError)
            } else if (value.length > 16) {
                Result.failure(LengthError)
            } else if (!value.matches("^(\\w+\\.?)*\\w+\$".toRegex())) {
                Result.failure(InvalidError)
            } else Result.success(value)
        } ?: Result.success(this.value)
    }

    class Key(value: String? = null) {
        object EmptyError : Error()
        object LengthError : Error()

        var value = value ?: ""
            private set

        val validation = value?.let {
            if (value.isEmpty()) {
                Result.failure(EmptyError)
            } else if (value.length > 32) {
                Result.failure(LengthError)
            } else Result.success(value)
        } ?: Result.success(this.value)
    }

    var id = Random.uuid()
    var name = Name()
    var key = Key()

    fun secretKey() = Key(PasswordEncoder.hash(EncoderSpec.Passphrase(
        PasswordEncoder.Base64.encode(key.value.encodeToByteArray()),
        PasswordEncoder.Base64.encode(name.value.encodeToByteArray())
    )))

    fun privateKey(secretKey: Key) = Key(PasswordEncoder.encrypt(
        PasswordEncoder.Base64.encode(key.value.encodeToByteArray()),
        EncoderSpec.Passphrase(secretKey.value, PasswordEncoder.Base64.encode(name.value.encodeToByteArray()))
    ))

    fun validateKey(key: Key) = if (key.value != this.key.value) {
        throw KeyInvalidError
    } else Unit
}