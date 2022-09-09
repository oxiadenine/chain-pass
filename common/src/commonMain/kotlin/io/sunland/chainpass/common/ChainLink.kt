package io.sunland.chainpass.common

class ChainLink constructor() {
    constructor(chainLink: ChainLink) : this() {
        id = chainLink.id
        name = chainLink.name
        description = chainLink.description
        password = chainLink.password
        status = chainLink.status
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

    enum class Status { ACTUAL, DRAFT, EDIT, SELECT }

    var id = 0
    var name = Name()
    var description = Description()
    var password = Password()
    var status = Status.DRAFT
}