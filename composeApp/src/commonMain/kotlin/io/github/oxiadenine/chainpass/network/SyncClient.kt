package io.github.oxiadenine.chainpass.network

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers

class SyncClient(private val hostAddress: String) {
    object SyncNetworkError : Error() {
        private fun readResolve(): Any = SyncNetworkError
    }

    private var selectorManager: SelectorManager? = null
    private var socket: Socket? = null

    suspend fun connect(): Socket {
        selectorManager = SelectorManager(Dispatchers.IO)
        socket = try {
            aSocket(selectorManager!!).tcp().connect(hostAddress, TcpSocket.PORT)
        } catch (_: Throwable) {
            throw SyncNetworkError
        }

        println("Sync client connected to ${hostAddress}:${TcpSocket.PORT}")

        return socket!!
    }

    fun disconnect() = try {
        socket?.close()
        selectorManager?.close()
    } catch (_: Throwable) {}
}