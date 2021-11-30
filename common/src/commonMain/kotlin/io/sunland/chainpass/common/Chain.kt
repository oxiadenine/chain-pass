package io.sunland.chainpass.common

enum class ChainStatus { ACTUAL, DRAFT, REMOVE, SELECT }

class Chain {
    class Name(value: String? = null) {
        var value = value ?: ""
            private set

        val isValid = value?.let { !(value.isEmpty() || value.length > 16) } ?: true
    }

    class Key(value: String? = null) {
        var value = value ?: ""
            private set

        var isValid = value?.let { !(value.isEmpty() || value.length > 32) } ?: true

        fun matches(key: String) = if (value != key) {
            throw IllegalArgumentException("Key is not valid")
        } else Unit
    }

    var id = 0
    var name = Name()
    var key = Key()
    var status = ChainStatus.DRAFT
}
