package io.sunland.chainpass.common.security

import java.security.SecureRandom
import java.util.*

actual object Random : SecureRandom() {
    actual override fun nextInt() = super.nextInt()
    actual fun nextInt(range: Pair<Int, Int>) = this.nextInts(1, range).first()

    actual fun nextInts(size: Int, range: Pair<Int, Int>) = super.ints(size.toLong(), range.first, range.second)
        .toArray()
        .toTypedArray()

    actual fun nextBytes(size: Int) = ByteArray(size).let { bytes ->
        super.nextBytes(bytes)

        bytes
    }

    actual fun uuid() = UUID.randomUUID().toString()
}