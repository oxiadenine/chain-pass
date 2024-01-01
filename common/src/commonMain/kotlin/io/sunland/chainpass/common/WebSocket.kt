package io.sunland.chainpass.common

import io.ktor.http.cio.websocket.*

enum class SocketConnectionType { SERVICE, CLIENT }

enum class SocketMessageType {
    CHAIN_CREATE,
    CHAIN_READ,
    CHAIN_DELETE,
    CHAIN_LINK_CREATE,
    CHAIN_LINK_READ,
    CHAIN_LINK_UPDATE,
    CHAIN_LINK_DELETE
}

data class SocketConnection(val type: SocketConnectionType, val socketId: String, val session: DefaultWebSocketSession)

data class SocketMessage(val type: SocketMessageType, val text: String = "", var socketId: String = "") {
    companion object {
        fun from(frame: Frame.Text): SocketMessage {
            val (type, text, socketId) = frame.readText().split("#")

            return SocketMessage(SocketMessageType.valueOf(type), text, socketId)
        }
    }

    fun toFrame() = Frame.Text("${type.name}#$text#$socketId")
}
