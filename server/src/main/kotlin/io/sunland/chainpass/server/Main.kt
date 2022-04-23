package io.sunland.chainpass.server

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.application
import androidx.compose.ui.window.isTraySupported
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, applicationEngineEnvironment {
        config = args.joinToString { env -> "application.$env" }.ifEmpty { "application" }.let { name ->
            HoconApplicationConfig(ConfigFactory.load(name))
        }

        module { main() }

        connector {
            host = config.property("server.host").getString()
            port = config.property("server.port").getString().toInt()
        }
    }).start()

    if (isTraySupported) {
        application {
            Tray(
                icon = painterResource("icon.png"),
                menu = {
                    Item("Exit", onClick = {
                        server.stop(0, 0)

                        exitApplication()
                    })
                }
            )
        }
    }
}
