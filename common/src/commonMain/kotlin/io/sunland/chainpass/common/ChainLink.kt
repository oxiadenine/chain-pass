package io.sunland.chainpass.common

class ChainLink {
    class Name(value: String? = null) {
        var value = value ?: ""
            private set

        val isValid = value?.let { value.isNotEmpty() && value.length <= 16 } ?: true
    }

    class Description(value: String? = null) {
        var value = value ?: ""
            private set

        val isValid = value?.let { value.length <= 24 } ?: true
    }

    class Password(value: String? = null) {
        var value = value ?: ""
            private set

        val isValid = value?.let { value.isNotEmpty() && value.length <= 32 } ?: true
    }

    enum class Status { ACTUAL, DRAFT, EDIT }

    var id = 0
    var name = Name()
    var description = Description()
    var password = Password()
    var status = Status.DRAFT
}
