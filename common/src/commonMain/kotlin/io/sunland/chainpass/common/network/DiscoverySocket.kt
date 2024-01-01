package io.sunland.chainpass.common.network

object DiscoverySocket {
    const val DNS = "8.8.8.8"
    const val HOST = "0.0.0.0"
    const val PORT = 8888
    const val MESSAGE = "DISCOVERY"
    const val TIMEOUT = 250
}

expect fun DiscoverySocket.getLocalHost(): String
expect suspend fun DiscoverySocket.discover(): String