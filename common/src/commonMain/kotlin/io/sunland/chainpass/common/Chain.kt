package io.sunland.chainpass.common

class Chain constructor() {
    constructor(chain: Chain) : this() {
        id = chain.id
        name = chain.name
        key = chain.key
        status = chain.status
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

        fun matches(key: String) = if (value != key) {
            Result.failure(IllegalArgumentException("Key is not valid"))
        } else Result.success(Unit)
    }

    enum class Status { ACTUAL, DRAFT }

    var id = 0
    var name = Name()
    var key = Key()
    var status = Status.DRAFT
}