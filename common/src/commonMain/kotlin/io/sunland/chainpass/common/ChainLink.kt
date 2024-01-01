package io.sunland.chainpass.common

import io.sunland.chainpass.common.security.EncoderSpec
import io.sunland.chainpass.common.security.PasswordEncoder
import io.sunland.chainpass.common.security.Random

class ChainLink(val chain: Chain) {
    constructor(chainLink: ChainLink) : this(chainLink.chain) {
        id = chainLink.id
        name = chainLink.name
        description = chainLink.description
        password = chainLink.password
    }

    class Name(value: String? = null) {
        object EmptyError : Error() {
            private fun readResolve(): Any = EmptyError
        }

        object LengthError : Error() {
            private fun readResolve(): Any = LengthError
        }

        object InvalidError : Error() {
            private fun readResolve(): Any = InvalidError
        }

        var value = value ?: ""
            private set

        val validation = value?.let {
            if (value.isEmpty()) {
                Result.failure(EmptyError)
            } else if (value.length > 16) {
                Result.failure(LengthError)
            } else if (!value.matches("^(\\w+\\.?)*\\w+\$".toRegex())) {
                Result.failure(InvalidError)
            }else Result.success(value)
        } ?: Result.success(this.value)
    }

    class Description(value: String? = null) {
        object LengthError : Error() {
            private fun readResolve(): Any = LengthError
        }

        var value = value ?: ""
            private set

        val validation = value?.let {
            if (value.length > 24) {
                Result.failure(LengthError)
            } else Result.success(value)
        } ?: Result.success(this.value)
    }

    class Password(value: String? = null) {
        object EmptyError : Error() {
            private fun readResolve(): Any = EmptyError
        }

        object LengthError : Error() {
            private fun readResolve(): Any = LengthError
        }

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
    var description = Description()
    var password = Password()

    fun privatePassword(secretKey: Chain.Key) = Password(PasswordEncoder.encrypt(
        PasswordEncoder.Base64.encode(password.value.encodeToByteArray()),
        EncoderSpec.Passphrase(secretKey.value, PasswordEncoder.Base64.encode(name.value.encodeToByteArray()))
    ))

    fun plainPassword(secretKey: Chain.Key) = Password(PasswordEncoder.Base64.decode(PasswordEncoder.decrypt(
        password.value, EncoderSpec.Passphrase(secretKey.value, PasswordEncoder.Base64.encode(name.value.encodeToByteArray()))
    )).decodeToString())
}