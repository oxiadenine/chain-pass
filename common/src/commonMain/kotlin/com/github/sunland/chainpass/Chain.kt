package com.github.sunland.chainpass

import com.github.sunland.chainpass.security.PasswordEncoder
import com.github.sunland.chainpass.security.Random

class Chain() {
    object KeyInvalidError : Error() {
        private fun readResolve(): Any = KeyInvalidError
    }

    constructor(chain: Chain) : this() {
        id = chain.id
        name = chain.name
        key = chain.key
        salt = chain.salt
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
            } else Result.success(value)
        } ?: Result.success(this.value)
    }

    class Key(value: String? = null) {
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
    var key = Key()
    var salt = ""

    fun secretKey() = Key(
        PasswordEncoder.hash(
        PasswordEncoder.Base64.encode(key.value.encodeToByteArray()), salt)
    )

    fun privateKey(secretKey: Key) = Key(
        PasswordEncoder.hash(
        PasswordEncoder.Base64.encode(key.value.encodeToByteArray()), secretKey.value)
    )

    fun validateKey(key: Key) = if (key.value != this.key.value) {
        throw KeyInvalidError
    } else Unit
}