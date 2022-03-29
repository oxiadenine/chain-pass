package io.sunland.chainpass.service

import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.cio.websocket.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import io.sunland.chainpass.common.network.SocketMessage
import io.sunland.chainpass.common.network.SocketRoute
import io.sunland.chainpass.common.repository.ChainEntity
import io.sunland.chainpass.common.repository.ChainKeyEntity
import io.sunland.chainpass.common.security.EncoderSpec
import io.sunland.chainpass.common.security.PasswordEncoder
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import kotlin.test.Test
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ChainWebSocketTest {
    private val appEngineEnv: ApplicationEngineEnvironment
        get() = createTestEnvironment {
            config = HoconApplicationConfig(ConfigFactory.load())

            module { main() }

            connector {
                host = config.property("server.host").getString()
                port = config.property("server.port").getString().toInt()
            }
        }

    private var chainEntity = ChainEntity(0, "test", "test")
    private var chainKeyEntity = ChainKeyEntity(chainEntity.id, chainEntity.key)

    private enum class Operation { CREATE, READ, DELETE }

    @Test
    @Order(1)
    fun testCreate() {
        withApplication(appEngineEnv) {
            handleWebSocketConversation(SocketRoute.CHAIN_CREATE.path) { incoming, outgoing ->
                chainEntity = ChainEntity(
                    chainEntity.id,
                    chainEntity.name,
                    PasswordEncoder.encrypt(
                        PasswordEncoder.Base64.encode(chainEntity.key.encodeToByteArray()),
                        EncoderSpec.Passphrase(
                            PasswordEncoder.hash(
                                EncoderSpec.Passphrase(
                                    PasswordEncoder.Base64.encode(chainEntity.key.encodeToByteArray()),
                                    PasswordEncoder.Base64.encode(chainEntity.name.encodeToByteArray())
                                )
                            ),
                            PasswordEncoder.Base64.encode(chainEntity.name.encodeToByteArray())
                        )
                    )
                )

                outgoing.send(SocketMessage.success(Json.encodeToString(chainEntity)).toFrame())

                val message = SocketMessage.from(incoming.receive() as Frame.Text)

                chainEntity.id = Json.decodeFromString<ChainEntity>(message.data.getOrThrow()).id

                assertTrue { chainEntity.id != 0 }
            }
        }

        checkTest(Operation.CREATE)
    }

    @Test
    @Order(2)
    fun testRead() = checkTest(Operation.READ)

    @Test
    @Order(3)
    fun testDelete() {
        getChainKey()

        withApplication(appEngineEnv) {
            handleWebSocketConversation(SocketRoute.CHAIN_DELETE.path) { incoming, outgoing ->
                chainKeyEntity = ChainKeyEntity(
                    chainKeyEntity.id,
                    PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, chainKeyEntity.key))
                )

                outgoing.send(SocketMessage.success(Json.encodeToString(chainKeyEntity)).toFrame())

                val message = SocketMessage.from(incoming.receive() as Frame.Text)

                message.data.getOrThrow()
            }
        }

        checkTest(Operation.DELETE)
    }

    private fun getChainKey() {
        withApplication(appEngineEnv) {
            handleWebSocketConversation(SocketRoute.CHAIN_KEY.path) { incoming, outgoing ->
                outgoing.send(SocketMessage.success(chainEntity.id.toString()).toFrame())

                val message = SocketMessage.from(incoming.receive() as Frame.Text)

                chainKeyEntity = Json.decodeFromString(message.data.getOrThrow())

                assertTrue { chainKeyEntity.key != "" }
            }
        }
    }

    private fun checkTest(operation: Operation) {
        withApplication(appEngineEnv) {
            handleWebSocketConversation(SocketRoute.CHAIN_READ.path) { incoming, outgoing ->
                outgoing.send(SocketMessage.success().toFrame())

                val message = SocketMessage.from(incoming.receive() as Frame.Text)

                val chainEntities = Json.decodeFromString<List<ChainEntity>>(message.data.getOrThrow())

                when (operation) {
                    Operation.CREATE -> assertTrue { chainEntities.first().id == chainEntity.id }
                    Operation.READ -> assertTrue { chainEntities.size == 1 }
                    Operation.DELETE -> assertTrue { chainEntities.isEmpty() }
                }
            }
        }
    }
}
