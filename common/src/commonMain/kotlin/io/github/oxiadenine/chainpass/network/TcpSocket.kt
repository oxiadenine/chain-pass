package io.github.oxiadenine.chainpass.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.net.DatagramSocket
import java.net.InetAddress

object TcpSocket {
    const val PORT = 8080

    enum class Route(val path: String) {
        CHAIN_SYNC("chains.sync"),
        CHAIN_LINK_SYNC("chain.links.sync")
    }

    @Serializable
    data class Payload(val route: String, val data: JsonElement? = null)

    val hostAddressFlow = flow {
        while (true) {
            emit(getLocalHost().getOrElse { "" })

            delay(5000)
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    private fun getLocalHost() = runCatching {
        val hostAddress = DatagramSocket().use { socket ->
            socket.connect(InetAddress.getByName("8.8.8.8"), 8888)
            socket.localAddress.hostAddress!!
        }

        if (hostAddress.contains("::")) "" else hostAddress
    }
}