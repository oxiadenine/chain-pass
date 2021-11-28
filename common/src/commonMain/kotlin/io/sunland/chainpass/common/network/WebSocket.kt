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

class SocketConnection(val type: SocketConnectionType, val socketId: String, val session: DefaultWebSocketSession)

class SocketMessage private constructor(val type: SocketMessageType, val data: Result<String>, val socketId: String = "") {
    companion object {
        fun success(type: SocketMessageType, data: String = "", socketId: String = "") =
            SocketMessage(type, Result.success(data), socketId)

        fun failure(type: SocketMessageType, message: String, socketId: String = "") =
            SocketMessage(type, Result.failure(Throwable(message)), socketId)

        fun from(frame: Frame.Text): SocketMessage {
            val frameText = frame.readText()

            if (!frameText.matches("^[^#|@]+#[^#|@]*#[01](@[^#|@]+)?$".toRegex())) {
                throw IllegalArgumentException("Invalid socket message")
            }

            return if (frameText.contains("@")) {
                val (type, text, status) = frameText.substringBefore("@").split("#")
                val socketId = frameText.substringAfter("@")

                if (status.toInt() == 1) {
                    success(SocketMessageType.valueOf(type), text, socketId)
                } else failure(SocketMessageType.valueOf(type), text, socketId)
            } else {
                val (type, text, status) = frameText.split("#")

                if (status.toInt() == 1) {
                    success(SocketMessageType.valueOf(type), text)
                } else failure(SocketMessageType.valueOf(type), text)
            }
        }
    }

    fun toFrame() = data.fold(
        onSuccess = { text -> Frame.Text("${type.name}#$text#1") },
        onFailure = { exception -> Frame.Text("${type.name}#${exception.message!!}#0") }
    )

    fun toFrame(socketId: String) = "${toFrame().readText()}@$socketId"
}
