package io.github.oxiadenine.chainpass.security

expect object Random {
    fun nextInt(): Int
    fun nextInt(range: Pair<Int, Int>): Int

    fun nextInts(size: Int, range: Pair<Int, Int>): Array<Int>

    fun nextBytes(size: Int): ByteArray

    fun uuid(): String
}