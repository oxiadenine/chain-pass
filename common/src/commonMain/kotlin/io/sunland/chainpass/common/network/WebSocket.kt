package io.sunland.chainpass.common.network

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

class SocketMessage(val type: SocketMessageType, val text: String = "") {
    var socketId: String? = null
        private set

    companion object {
        fun from(frame: Frame.Text): SocketMessage {
            val messageParts = frame.readText().split("#")

            return if (messageParts.size <= 1) {
                throw IllegalArgumentException("Invalid ${::SocketMessage.name}(type, text)")
            } else if (messageParts.size < 3) {
                SocketMessage(SocketMessageType.valueOf(messageParts[0]), messageParts[1])
            } else SocketMessage(SocketMessageType.valueOf(messageParts[0]), messageParts[1]).also { message ->
                message.socketId = messageParts[2]
            }
        }
    }

    fun toFrame(socketId: String? = null)  = socketId?.let {
        Frame.Text("${type.name}#$text#$socketId")
    } ?: Frame.Text("${type.name}#$text")
}
