package io.sunland.chainpass.service

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import io.ktor.websocket.WebSockets
import io.sunland.chainpass.common.SocketConnection
import io.sunland.chainpass.common.SocketMessage
import io.sunland.chainpass.common.SocketType
import kotlinx.coroutines.isActive
import org.slf4j.event.Level
import java.util.*

fun Application.main() {
    install(WebSockets)
    install(CallLogging) { level = Level.INFO }

    val socketConnections = Collections.synchronizedSet<SocketConnection>(LinkedHashSet())

    routing {
        webSocket("/") {
            val fromConnection = SocketConnection(
                SocketType.valueOf(call.request.headers["Socket-Type"]!!),
                call.request.headers["Socket-Id"]!!,
                this
            )
            socketConnections += fromConnection

            log.info("${fromConnection.type.name}#${fromConnection.id}")

            for (frame in incoming) {
                val message = SocketMessage.from(frame as Frame.Text)

                val toConnection = when (fromConnection.type) {
                    SocketType.SERVICE -> socketConnections.first { connection ->
                        connection.type == SocketType.CLIENT && connection.id == message.id && connection.session.isActive
                    }
                    else -> socketConnections.first { connection ->
                        connection.type == SocketType.SERVICE && connection.session.isActive
                    }
                }

                toConnection.session.send(message.toFrame())

                log.info("${fromConnection.type.name}#${fromConnection.id} -> ${toConnection.type.name}#${toConnection.id}")
            }

            socketConnections.removeAll { !it.session.isActive }
        }
    }
}
