package io.sunland.chainpass.common

enum class ChainLinkStatus { ACTUAL, DRAFT, EDIT }

class ChainLink {
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
    var password: String = ""
        set(value) {
            if (value.isEmpty()) {
                throw IllegalArgumentException("${::password.name} can't be empty")
            }

            if (value.length > 32) {
                throw IllegalArgumentException("${::password.name} length can't be greater than 32")
            }

            field = value
        }
    var status: ChainLinkStatus = ChainLinkStatus.DRAFT
    var chainId: Int = 0
}
