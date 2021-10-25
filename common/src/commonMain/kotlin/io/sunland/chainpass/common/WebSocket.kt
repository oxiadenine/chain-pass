package io.sunland.chainpass.common

import io.ktor.http.cio.websocket.*

object WebSocket {
    enum class Type { CLIENT, SERVICE }

    data class Connection(val session: DefaultWebSocketSession, val type: Type)

    data class Message(val text: String, val type: Type) {
        companion object {
            fun from(frame: Frame.Text): Message {
                val (text, type) = frame.readText().split("#")

                return Message(text, Type.valueOf(type))
            }
        }

        fun toFrame() = Frame.Text("$text#${type.name}")
    }
}
