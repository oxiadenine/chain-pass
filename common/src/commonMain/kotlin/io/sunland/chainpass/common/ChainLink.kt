package io.sunland.chainpass.common

import io.sunland.chainpass.common.security.PasswordEncoder

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

    fun encryptPassword(key: String) = apply { password = PasswordEncoder.encrypt(key, name, password) }
    fun decryptPassword(key: String) = apply { password = PasswordEncoder.decrypt(key, name, password) }
}
