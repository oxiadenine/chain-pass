package io.sunland.chainpass.service

import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    embeddedServer(Netty, applicationEngineEnvironment {
        config = args.joinToString { env -> "application.$env" }.ifEmpty { "application" }.let { name ->
            HoconApplicationConfig(ConfigFactory.load(name))
        }

        module {
            main()
        }

        connector {
            host = config.property("server.host").getString()
            port = config.property("server.port").getString().toInt()
        }
    }).start()
}
