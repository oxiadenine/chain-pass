package io.sunland.chainpass.common

enum class ChainStatus { ACTUAL, DRAFT }

class Chain {
    class Key(val value: String) {
        fun validate(key: String) = if (value != key) {
            throw IllegalArgumentException("${::Key.name} values doesn't match")
        } else Unit
    }

    var id: Int = 0
    var name: String = ""
        set(value) {
            field = value

            if (value.isEmpty()) {
                throw IllegalArgumentException("${::name.name} can't be empty")
            }

            if (value.length > 16) {
                throw IllegalArgumentException("${::name.name} length can't be greater than 16")
            }
        }
    var key: Key = Key("")
        private set
    var status: ChainStatus = ChainStatus.DRAFT

    fun setKey(value: String) {
        key = Key(value)

        if (value.isEmpty()) {
            throw IllegalArgumentException("${::key.name} can't be empty")
        }

        if (value.length > 32) {
            throw IllegalArgumentException("${::key.name} length can't be greater than 32")
        }
    }
}
