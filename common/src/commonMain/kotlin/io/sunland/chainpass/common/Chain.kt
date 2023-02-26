package io.sunland.chainpass.common

import io.sunland.chainpass.common.security.EncoderSpec
import io.sunland.chainpass.common.security.PasswordEncoder
import io.sunland.chainpass.common.security.PasswordGenerator
import io.sunland.chainpass.common.security.Random

class Chain constructor(val passwordGenerator: PasswordGenerator) {
    constructor(chain: Chain) : this(chain.passwordGenerator) {
        id = chain.id
        name = chain.name
        key = chain.key
        isLatest = chain.isLatest
    }

    class Name(value: String? = null) {
        var value = value ?: ""
            private set

        val validation = value?.let {
            if (value.isEmpty()) {
                Result.failure(IllegalArgumentException("Name is empty"))
            } else if (value.length > 16) {
                Result.failure(IllegalArgumentException("Name is too long"))
            } else Result.success(value)
        } ?: Result.success(this.value)
    }

    class Key(value: String? = null) {
        var value = value ?: ""
            private set

        val validation = value?.let {
            if (value.isEmpty()) {
                Result.failure(IllegalArgumentException("Key is empty"))
            } else if (value.length > 32) {
                Result.failure(IllegalArgumentException("Key is too long"))
            } else Result.success(value)
        } ?: Result.success(this.value)
    }

    var id = Random.uuid()
    var name = Name()
    var key = Key()
    var isSelected = false
    var isLatest = false

    fun generateKey() = passwordGenerator.generate()

    fun secretKey() = Key(PasswordEncoder.hash(EncoderSpec.Passphrase(
        PasswordEncoder.Base64.encode(key.value.encodeToByteArray()),
        PasswordEncoder.Base64.encode(name.value.encodeToByteArray())
    )))

    fun privateKey(secretKey: Key) = Key(PasswordEncoder.encrypt(
        PasswordEncoder.Base64.encode(key.value.encodeToByteArray()),
        EncoderSpec.Passphrase(secretKey.value, PasswordEncoder.Base64.encode(name.value.encodeToByteArray()))
    ))

    fun validateKey(key: Key) = if (key.value != this.key.value) {
        throw IllegalArgumentException("Key is not valid")
    } else Unit
}