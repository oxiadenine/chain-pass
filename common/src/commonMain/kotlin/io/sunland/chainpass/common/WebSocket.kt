package io.sunland.chainpass.common

import io.ktor.http.cio.websocket.*

object WebSocket {
    enum class ConnectionType { CLIENT, SERVICE }
    enum class MessageType {
        CREATE_CHAIN,
        READ_CHAIN,
        DELETE_CHAIN,
        CREATE_CHAIN_LINK,
        READ_CHAIN_LINK,
        UPDATE_CHAIN_LINK,
        DELETE_CHAIN_LINK
    }

    data class Connection(val session: DefaultWebSocketSession, val type: ConnectionType)

    data class Message(val text: String, val type: MessageType) {
        companion object {
            fun from(frame: Frame.Text): Message {
                val (text, type) = frame.readText().split("#")

                return Message(text, MessageType.valueOf(type))
            }
        }

        fun toFrame() = Frame.Text("$text#${type.name}")
    }
}
