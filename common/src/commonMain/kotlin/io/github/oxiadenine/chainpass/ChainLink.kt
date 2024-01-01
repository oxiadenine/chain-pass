package io.github.oxiadenine.chainpass

import io.github.oxiadenine.chainpass.security.PasswordEncoder
import io.github.oxiadenine.chainpass.security.Random

class ChainLink(val chain: Chain) {
    constructor(chainLink: ChainLink) : this(chainLink.chain) {
        id = chainLink.id
        name = chainLink.name
        description = chainLink.description
        password = chainLink.password
        iv = chainLink.iv
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
    var iv = ""

    fun privatePassword(secretKey: Chain.Key) = Password(
        PasswordEncoder.encrypt(
        PasswordEncoder.Base64.encode(password.value.encodeToByteArray()), secretKey.value, iv)
    )

    fun plainPassword(secretKey: Chain.Key) = Password(
        PasswordEncoder.Base64.decode(
        PasswordEncoder.decrypt(password.value, secretKey.value, iv)
    ).decodeToString())
}