package io.sunland.chainpass.common

import io.ktor.http.cio.websocket.*

enum class SocketType { SERVICE, CLIENT }

enum class SocketMessageType {
    CREATE_CHAIN,
    READ_CHAIN,
    DELETE_CHAIN,
    CREATE_CHAIN_LINK,
    READ_CHAIN_LINK,
    UPDATE_CHAIN_LINK,
    DELETE_CHAIN_LINK
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
