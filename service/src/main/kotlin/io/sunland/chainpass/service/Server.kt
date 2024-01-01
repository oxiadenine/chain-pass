package io.sunland.chainpass.service

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import io.ktor.websocket.WebSockets
import io.sunland.chainpass.common.network.SocketConnection
import io.sunland.chainpass.common.network.SocketMessage
import io.sunland.chainpass.common.network.SocketConnectionType
import kotlinx.coroutines.isActive
import org.slf4j.event.Level
import java.util.*

fun Application.main() {
    install(WebSockets)
    install(CallLogging) { level = Level.INFO }

    val socketConnections = Collections.synchronizedSet<SocketConnection>(LinkedHashSet())

    routing {
        webSocket("/") {
            call.request.headers["Socket-Type"]?.let { socketType ->
                val fromConnection = SocketConnection(
                    SocketConnectionType.valueOf(socketType),
                    UUID.randomUUID().toString(),
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
                                connection.type == SocketConnectionType.CLIENT &&
                                        connection.socketId == message.socketId &&
                                        connection.session.isActive
                            }
                            SocketConnectionType.CLIENT -> socketConnections.first { connection ->
                                connection.type == SocketConnectionType.SERVICE && connection.session.isActive
                            }
                        }

                        toConnection.session.send(message.toFrame(fromConnection.socketId))
                    }
                }.onFailure { exception -> log.info(exception.message) }

                if (fromConnection.type == SocketConnectionType.CLIENT) {
                    fromConnection.session.close()

                    socketConnections.remove(fromConnection)
                }
            } ?: close()
        }
    }
}
