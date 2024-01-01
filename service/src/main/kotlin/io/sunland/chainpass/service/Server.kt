package io.sunland.chainpass.service

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import io.ktor.websocket.WebSockets
import io.sunland.chainpass.common.WebSocket
import kotlinx.coroutines.isActive
import org.slf4j.event.Level
import java.util.*

fun Application.main() {
    install(WebSockets)
    install(CallLogging) { level = Level.INFO }

    val connections = Collections.synchronizedSet<WebSocket.Connection?>(LinkedHashSet())

    routing {
        webSocket("/") {
            val fromConnection = WebSocket.Connection(
                this,
                WebSocket.ConnectionType.valueOf(call.request.headers["Socket-Type"]!!),
            )
            connections += fromConnection

            log.info(fromConnection.type.name)

            for (frame in incoming) {
                val message = WebSocket.Message.from(frame as Frame.Text)

                val toConnection = when (fromConnection.type) {
                    WebSocket.ConnectionType.SERVICE -> connections.first {
                        it.type == WebSocket.ConnectionType.CLIENT && it.session.isActive
                    }
                    else -> connections.first {
                        it.type == WebSocket.ConnectionType.SERVICE && it.session.isActive
                    }
                }

                toConnection.session.send(message.toFrame())

                log.info("${fromConnection.type.name} -> ${toConnection.type.name}")
            }

            connections.removeAll { !it.session.isActive }
        }
    }
}
