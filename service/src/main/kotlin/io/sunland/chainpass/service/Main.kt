package io.sunland.chainpass.service

import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, applicationEngineEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load())

        module {
            main()
        }

        connector {
            host = config.property("server.host").getString()
            port = config.property("server.port").getString().toInt()
        }
    }).start()
}
