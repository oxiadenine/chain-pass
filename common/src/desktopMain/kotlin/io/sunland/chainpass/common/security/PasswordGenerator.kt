package io.sunland.chainpass.common.security

import java.security.SecureRandom

actual object RandomInt : SecureRandom() {
    actual fun next() = super.nextInt()
    actual fun next(range: Pair<Int, Int>) = super.nextInt(range.first, range.second)

    actual fun nextArray(size: Int, range: Pair<Int, Int>) = super.ints(size.toLong(), range.first, range.second)
        .toArray()
        .toTypedArray()
}