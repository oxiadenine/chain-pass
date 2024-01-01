package io.sunland.chainpass.common

enum class ChainLinkStatus { ACTUAL, DRAFT, EDIT }

class ChainLink {
    var id: Int = 0
    var name: String = ""
        set(value) {
            field = value

            if (value.isEmpty() || value.length > 16) {
                throw IllegalArgumentException("Name can't be empty or greater than 16 characters")
            }
        }
    var password: String = ""
        set(value) {
            field = value

            if (value.isEmpty() || value.length > 32) {
                throw IllegalArgumentException("Password can't be empty or greater than 32 characters")
            }
        }
    var status: ChainLinkStatus = ChainLinkStatus.DRAFT
}
