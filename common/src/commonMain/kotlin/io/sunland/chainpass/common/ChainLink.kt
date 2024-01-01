package io.sunland.chainpass.common

import io.sunland.chainpass.common.security.EncoderSpec
import io.sunland.chainpass.common.security.PasswordEncoder

class ChainLink(val chain: Chain) {
    constructor(chainLink: ChainLink) : this(chainLink.chain) {
        id = chainLink.id
        name = chainLink.name
        description = chainLink.description
        password = chainLink.password
        status = chainLink.status
        isLatest = chainLink.isLatest
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

    class Description(value: String? = null, val isEdited: Boolean = false) {
        var value = value ?: ""
            private set

        val validation = value?.let {
            if (value.length > 24) {
                Result.failure(IllegalArgumentException("Description is too long"))
            } else Result.success(value)
        } ?: Result.success(this.value)
    }

    class Password(value: String? = null, val isPrivate: Boolean = true, val isEdited: Boolean = false) {
        var value = value ?: ""
            private set

        val validation = value?.let {
            if (value.isEmpty()) {
                Result.failure(IllegalArgumentException("Password is empty"))
            } else if (value.length > 16) {
                Result.failure(IllegalArgumentException("Password is too long"))
            } else Result.success(value)
        } ?: Result.success(this.value)
    }

    enum class Status { ACTUAL, DRAFT, EDIT }

    var id = 0
    var name = Name()
    var description = Description()
    var password = Password()
    var status = Status.DRAFT
    var isLatest = false

    fun unlockPassword(): Password {
        val secretKey = PasswordEncoder.hash(
            EncoderSpec.Passphrase(
                PasswordEncoder.Base64.encode(chain.key.value.encodeToByteArray()),
                PasswordEncoder.Base64.encode(chain.name.value.encodeToByteArray())
            ))

        val password = PasswordEncoder.decrypt(
            password.value,
            EncoderSpec.Passphrase(secretKey, PasswordEncoder.Base64.encode(name.value.encodeToByteArray()))
        )

        return Password(PasswordEncoder.Base64.decode(password).decodeToString(), false)
    }
}