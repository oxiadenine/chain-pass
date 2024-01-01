package io.sunland.chainpass.common

enum class ChainStatus { ACTUAL, DRAFT }

class Chain {
    var id: Int = 0
    var name: String = ""
        set(value) {
            if (value.isEmpty()) {
                throw IllegalArgumentException("${::name.name} can't be empty")
            }

            if (value.length > 16) {
                throw IllegalArgumentException("${::name.name} length can't be greater than 16")
            }

            field = value
        }
    var key: String = ""
        set(value) {
            if (value.isEmpty()) {
                throw IllegalArgumentException("${::key.name} can't be empty")
            }

            if (value.length > 32) {
                throw IllegalArgumentException("${::key.name} length can't be greater than 32")
            }

            field = value
        }
    var status: ChainStatus = ChainStatus.DRAFT

    fun validateKey(key: String) = if (this.key != key) {
        throw IllegalArgumentException("${this::key.name} values doesn't match")
    } else Unit
}
