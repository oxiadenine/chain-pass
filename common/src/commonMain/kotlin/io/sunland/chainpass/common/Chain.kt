package io.sunland.chainpass.common

class Chain {
    class Name(value: String? = null) {
        var value = value ?: ""
            private set

        val isValid = value?.let { value.isNotEmpty() && value.length <= 16 } ?: true
    }

    class Key(value: String? = null) {
        var value = value ?: ""
            private set

        val isValid = value?.let { value.isNotEmpty() && value.length <= 32 } ?: true

        fun matches(key: String) = if (value != key) {
            throw IllegalArgumentException("Key is not valid")
        } else Unit
    }

    enum class Status { ACTUAL, DRAFT }

    var id = 0
    var name = Name()
    var key = Key()
    var status = Status.DRAFT
}
