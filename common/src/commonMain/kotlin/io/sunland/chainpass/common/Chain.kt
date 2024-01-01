package io.sunland.chainpass.common

enum class ChainStatus { ACTUAL, DRAFT }

class Chain {
    class Key(value: String) {
        var value: String = value
            private set

        fun validate(key: String) = if (value != key) {
            throw IllegalArgumentException("Key is not valid")
        } else Unit

        fun clear() = apply { value = "" }
    }

    var id: Int = 0
    var name: String = ""
        set(value) {
            field = value

            if (value.isEmpty() || value.length > 16) {
                throw IllegalArgumentException("Name can't be empty or greater than 16 characters")
            }
        }
    var key: Key = Key("")
        private set
    var status: ChainStatus = ChainStatus.DRAFT

    fun setKey(value: String) {
        key = Key(value)

        if (value.isEmpty() || value.length > 32) {
            throw IllegalArgumentException("Key can't be empty or greater than 32 characters")
        }
    }
}
