package io.sunland.chainpass.common

import io.ktor.http.cio.websocket.*

enum class SocketType { SERVICE, CLIENT }

enum class SocketMessageType {
    CHAIN_CREATE,
    CHAIN_READ,
    CHAIN_DELETE,
    CHAIN_LINK_CREATE,
    CHAIN_LINK_READ,
    CHAIN_LINK_UPDATE,
    CHAIN_LINK_DELETE
}

data class SocketConnection(val type: SocketType, val id: String, val session: DefaultWebSocketSession, )

data class SocketMessage(val type: SocketMessageType, val id: String, val text: String = "") {
    companion object {
        fun from(frame: Frame.Text): SocketMessage {
            val (type, id, text) = frame.readText().split("#")

            return SocketMessage(SocketMessageType.valueOf(type), id, text)
        }
    }

    fun toFrame() = Frame.Text("${type.name}#$id#$text")
}

expect fun socketId(): String
