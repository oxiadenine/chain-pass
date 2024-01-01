package io.sunland.chainpass.server

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.application
import androidx.compose.ui.window.isTraySupported
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.sunland.chainpass.common.network.DiscoverySocket
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

fun main(args: Array<String>) {
    val appConfig = args.joinToString { env -> "application.$env" }.ifEmpty { "application" }
        .let { name -> ConfigFactory.load(name) }
        .let { config ->
            if (config.getString("server.discoveryAddress").isEmpty()) {
                config.withValue("server.discoveryAddress", ConfigValueFactory.fromAnyRef(
                    "${DiscoverySocket.getLocalHost()}:${config.getString("server.port")}")
                )
            } else config
        }.let { config -> HoconApplicationConfig(config) }

    val server = embeddedServer(Netty, applicationEngineEnvironment {
        config = appConfig

        module {
            main()
            discovery()
        }

        connector {
            host = config.property("server.host").getString()
            port = config.property("server.port").getString().toInt()
        }
    }).start()

    if (isTraySupported) {
        application {
            val discoveryAddress = appConfig.property("server.discoveryAddress").getString()

            Tray(
                icon = painterResource("icon.png"),
                menu = {
                    Item(text = discoveryAddress, onClick = {
                        Toolkit.getDefaultToolkit().systemClipboard
                            .setContents(StringSelection(discoveryAddress), null)
                    })
                    Item(text = "Exit", onClick = {
                        server.stop(0, 0)

                        exitApplication()
                    })
                }
            )
        }
    }
}