package io.sunland.chainpass.common.network

import io.ktor.client.engine.cio.*
import io.ktor.utils.io.core.*
import io.rsocket.kotlin.ConnectionAcceptorContext
import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.core.RSocketConnector
import io.rsocket.kotlin.core.RSocketServer
import io.rsocket.kotlin.metadata.RoutingMetadata
import io.rsocket.kotlin.metadata.metadata
import io.rsocket.kotlin.metadata.read
import io.rsocket.kotlin.payload.Payload
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import io.rsocket.kotlin.transport.ktor.websocket.client.WebSocketClientTransport
import io.rsocket.kotlin.transport.ktor.websocket.server.WebSocketServerTransport
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.DatagramSocket
import java.net.InetAddress

object WebSocket {
    private const val PORT = 8080

    enum class Route(val path: String) {
        CHAIN_SYNC("chains.sync"),
        CHAIN_LINK_SYNC("chain.links.sync")
    }

    suspend fun getLocalHost() = withContext(Dispatchers.IO) {
        DatagramSocket().use { socket ->
            socket.connect(InetAddress.getByName("8.8.8.8"), 8888)
            socket.localAddress.hostAddress!!
        }
    }

    suspend fun connect(socketHost: String): RSocket {
        val transport = WebSocketClientTransport(CIO, socketHost, PORT)
        val connector = RSocketConnector()

        return connector.connect(transport)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun start(socketHost: String, acceptor: ConnectionAcceptorContext.() -> RSocket) {
        val transport = WebSocketServerTransport(io.ktor.server.cio.CIO, PORT, socketHost)
        val connector = RSocketServer()

        connector.bind(transport, acceptor)
    }
}

@OptIn(ExperimentalMetadataApi::class)
fun Payload.getRoute(): WebSocket.Route = metadata?.read(RoutingMetadata)?.tags?.firstOrNull()?.let { path ->
    WebSocket.Route.values().first { route -> route.path == path }
} ?: throw IllegalStateException("No payload route provided")

@OptIn(ExperimentalMetadataApi::class)
fun Payload.Companion.encode(route: WebSocket.Route): Payload = buildPayload {
    data(ByteReadPacket.Empty)
    metadata(RoutingMetadata(route.path))
}

@OptIn(ExperimentalMetadataApi::class)
inline fun <reified T> Payload.Companion.encode(route: WebSocket.Route, value: T): Payload = buildPayload {
    data(Json.encodeToString(value))
    metadata(RoutingMetadata(route.path))
}

inline fun <reified T> Payload.Companion.encode(value: T): Payload = buildPayload {
    data(Json.encodeToString(value))
}

inline fun <reified T> Payload.decode(): T = Json.decodeFromString(data.readText())