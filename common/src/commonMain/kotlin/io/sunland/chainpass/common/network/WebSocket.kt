package io.sunland.chainpass.common.network

import io.ktor.http.cio.websocket.*

enum class SocketRoute(val path: String) {
    CHAIN_CREATE("/chain/create"),
    CHAIN_READ("/chain/read"),
    CHAIN_DELETE("/chain/delete"),
    CHAIN_KEY("/chain/key"),
    CHAIN_LINK_CREATE("/chain/link/create"),
    CHAIN_LINK_READ("/chain/link/read"),
    CHAIN_LINK_UPDATE("/chain/link/update"),
    CHAIN_LINK_DELETE("/chain/link/delete"),
}

class SocketMessage private constructor(val data: Result<String>) {
    companion object {
        fun success(data: String = "") = SocketMessage(Result.success(data))
        fun failure(message: String?) = SocketMessage(Result.failure(Throwable(message)))

        fun from(frame: Frame.Text): SocketMessage {
            val frameText = frame.readText()

            if (!frameText.matches("^[^@]*@[a-z]+$".toRegex())) {
                throw IllegalArgumentException("Invalid socket message")
            }

            val (text, status) = frameText.split("@")

            return if (status.toBoolean()) success(text) else failure(text)
        }
    }

    fun toFrame() = data.fold(
        onSuccess = { text -> Frame.Text("$text@true") },
        onFailure = { exception -> Frame.Text("${exception.message}@false") }
    )
}
