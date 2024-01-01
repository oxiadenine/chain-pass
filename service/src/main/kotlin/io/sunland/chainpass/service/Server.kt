package io.sunland.chainpass.service

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import io.ktor.websocket.WebSockets
import io.sunland.chainpass.common.SocketConnection
import io.sunland.chainpass.common.SocketMessage
import io.sunland.chainpass.common.SocketConnectionType
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
                SocketConnectionType.valueOf(call.request.headers["Socket-Type"]!!),
                call.request.headers["Socket-Id"]!!,
                this
            )

            if (fromConnection.type == SocketConnectionType.SERVICE) {
                if (socketConnections.none { connection -> connection.type == fromConnection.type }) {
                    socketConnections.add(fromConnection)
                } else fromConnection.session.close()
            } else socketConnections.add(fromConnection)

            log.info("${fromConnection.type.name}#${fromConnection.socketId}")

            runCatching {
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue

                    val message = SocketMessage.from(frame)

                    val toConnection = when (fromConnection.type) {
                        SocketConnectionType.SERVICE -> socketConnections.first { connection ->
                            connection.type == SocketConnectionType.CLIENT && connection.socketId == message.socketId &&
                                    connection.session.isActive
                        }
                        SocketConnectionType.CLIENT -> {
                            val serviceConnection = socketConnections.first { connection ->
                                connection.type == SocketConnectionType.SERVICE && connection.session.isActive
                            }

                            message.socketId = fromConnection.socketId

                            serviceConnection
                        }
                    }

                    toConnection.session.send(message.toFrame())
                }
            }.onFailure { exception -> log.info(exception.message) }

            if (fromConnection.type == SocketConnectionType.CLIENT) {
                socketConnections.remove(fromConnection)
            }
        }
    }
}
