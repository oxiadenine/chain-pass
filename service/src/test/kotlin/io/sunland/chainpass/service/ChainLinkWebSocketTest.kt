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
import io.sunland.chainpass.common.repository.ChainLinkEntity
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
class ChainLinkWebSocketTest {
    private val appEngineEnv: ApplicationEngineEnvironment
        get() = createTestEnvironment {
            config = HoconApplicationConfig(ConfigFactory.load())

            module { main() }

            connector {
                host = config.property("server.host").getString()
                port = config.property("server.port").getString().toInt()
            }
        }

    private val secretKey = PasswordEncoder.hash(EncoderSpec.Passphrase(
        PasswordEncoder.Base64.encode("test".encodeToByteArray()),
        PasswordEncoder.Base64.encode("test".encodeToByteArray())
    ))

    private var chainEntity = ChainEntity(0, "test", "test")
    private var chainKeyEntity = ChainKeyEntity(chainEntity.id, chainEntity.key)
    private var chainLinkEntity = ChainLinkEntity(0, "test", "test", "test", chainKeyEntity)

    private enum class Operation { CREATE, READ, UPDATE, DELETE }

    @BeforeAll
    fun createChain() {
        withApplication(appEngineEnv) {
            handleWebSocketConversation(SocketRoute.CHAIN_CREATE.path) { incoming, outgoing ->
                chainEntity = ChainEntity(
                    chainEntity.id,
                    chainEntity.name,
                    PasswordEncoder.encrypt(
                        PasswordEncoder.Base64.encode(chainEntity.key.encodeToByteArray()),
                        EncoderSpec.Passphrase(
                            secretKey,
                            PasswordEncoder.Base64.encode(chainEntity.name.encodeToByteArray())
                        )
                    )
                )

                outgoing.send(SocketMessage.success(Json.encodeToString(chainEntity)).toFrame())

                val message = SocketMessage.from(incoming.receive() as Frame.Text)

                chainEntity.id = Json.decodeFromString<ChainEntity>(message.data.getOrThrow()).id
            }
        }
    }

    @AfterAll
    fun deleteChain() {
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
    }

    @Test
    @Order(1)
    fun testCreate() {
        getChainKey()

        withApplication(appEngineEnv) {
            handleWebSocketConversation(SocketRoute.CHAIN_LINK_CREATE.path) { incoming, outgoing ->
                chainLinkEntity = ChainLinkEntity(
                    chainLinkEntity.id,
                    chainLinkEntity.name,
                    chainLinkEntity.description,
                    PasswordEncoder.encrypt(
                        PasswordEncoder.Base64.encode(chainLinkEntity.password.encodeToByteArray()),
                        EncoderSpec.Passphrase(
                            secretKey,
                            PasswordEncoder.Base64.encode(chainLinkEntity.name.encodeToByteArray())
                        )
                    ),
                    ChainKeyEntity(
                        chainKeyEntity.id,
                        PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, chainKeyEntity.key))
                    )
                )

                outgoing.send(SocketMessage.success(Json.encodeToString(chainLinkEntity)).toFrame())

                val message = SocketMessage.from(incoming.receive() as Frame.Text)

                chainLinkEntity.id = Json.decodeFromString<ChainLinkEntity>(message.data.getOrThrow()).id

                assertTrue { chainLinkEntity.id != 0 }
            }
        }

        checkTest(Operation.CREATE)
    }

    @Test
    @Order(2)
    fun testRead() = checkTest(Operation.READ)

    @Test
    @Order(3)
    fun testUpdate() {
        getChainKey()

        withApplication(appEngineEnv) {
            handleWebSocketConversation(SocketRoute.CHAIN_LINK_UPDATE.path) { incoming, outgoing ->
                chainLinkEntity = ChainLinkEntity(
                    chainLinkEntity.id,
                    chainLinkEntity.name,
                    chainLinkEntity.description,
                    PasswordEncoder.encrypt(
                        PasswordEncoder.Base64.encode("test".encodeToByteArray()),
                        EncoderSpec.Passphrase(
                            secretKey,
                            PasswordEncoder.Base64.encode(chainLinkEntity.name.encodeToByteArray())
                        )
                    ),
                    ChainKeyEntity(
                        chainKeyEntity.id,
                        PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, chainKeyEntity.key))
                    )
                )

                outgoing.send(SocketMessage.success(Json.encodeToString(chainLinkEntity)).toFrame())

                val message = SocketMessage.from(incoming.receive() as Frame.Text)

                message.data.getOrThrow()
            }
        }

        checkTest(Operation.UPDATE)
    }

    @Test
    @Order(4)
    fun testDelete() {
        getChainKey()

        withApplication(appEngineEnv) {
            handleWebSocketConversation(SocketRoute.CHAIN_LINK_DELETE.path) { incoming, outgoing ->
                chainLinkEntity = ChainLinkEntity(
                    chainLinkEntity.id,
                    chainLinkEntity.name,
                    chainLinkEntity.description,
                    chainLinkEntity.password,
                    ChainKeyEntity(
                        chainKeyEntity.id,
                        PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, chainKeyEntity.key))
                    )
                )

                outgoing.send(SocketMessage.success(Json.encodeToString(chainLinkEntity)).toFrame())

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
        getChainKey()

        withApplication(appEngineEnv) {
            withApplication(appEngineEnv) {
                handleWebSocketConversation(SocketRoute.CHAIN_LINK_READ.path) { incoming, outgoing ->
                    chainKeyEntity = ChainKeyEntity(
                        chainKeyEntity.id,
                        PasswordEncoder.hash(EncoderSpec.Passphrase(chainEntity.key, chainKeyEntity.key))
                    )

                    outgoing.send(SocketMessage.success(Json.encodeToString(chainKeyEntity)).toFrame())

                    val message = SocketMessage.from(incoming.receive() as Frame.Text)

                    val chainLinkEntities = Json.decodeFromString<List<ChainLinkEntity>>(message.data.getOrThrow())

                    when (operation) {
                        Operation.CREATE -> assertTrue { chainLinkEntities.first().id == chainLinkEntity.id }
                        Operation.READ -> assertTrue { chainLinkEntities.size == 1 }
                        Operation.UPDATE -> assertTrue { chainLinkEntities.first().password == chainLinkEntity.password }
                        Operation.DELETE -> assertTrue { chainLinkEntities.isEmpty() }
                    }
                }
            }
        }
    }
}
