package io.github.oxiadenine.chainpass.network

import io.github.oxiadenine.chainpass.repository.ChainEntity
import io.github.oxiadenine.chainpass.repository.ChainLinkEntity
import io.github.oxiadenine.chainpass.repository.ChainLinkRepository
import io.github.oxiadenine.chainpass.repository.ChainRepository
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

class SyncServer(
    private val chainRepository: ChainRepository,
    private val chainLinkRepository: ChainLinkRepository
) {
    private var selectorManager: SelectorManager? = null
    private var serverSocket: ServerSocket? = null

    suspend fun start(hostAddress: String) = coroutineScope {
        selectorManager = SelectorManager(Dispatchers.IO)
        serverSocket = aSocket(selectorManager!!).tcp().bind(hostAddress, TcpSocket.PORT)

        println("Sync server started at ${hostAddress}:${TcpSocket.PORT}")

        while (true) {
            val socket = serverSocket!!.accept()

            val readChannel = socket.openReadChannel()
            val writeChannel = socket.openWriteChannel(autoFlush = true)

            val readPayload = Json.decodeFromString<TcpSocket.Payload>(readChannel.readUTF8Line()!!)

            val writePayload = when (TcpSocket.Route.valueOf(readPayload.route)) {
                TcpSocket.Route.CHAIN_SYNC -> {
                    val chainEntityMap = mutableMapOf<ChainEntity, List<ChainLinkEntity>>()

                    chainRepository.getAll().forEach { chainEntity ->
                        chainEntityMap[chainEntity] = chainLinkRepository.getBy(chainEntity.id)
                    }

                    val chainJsonArray = buildJsonArray {
                        chainEntityMap.map { (chainEntity, chainLinkEntities) ->
                            addJsonObject {
                                put("id", chainEntity.id)
                                put("name", chainEntity.name)
                                put("key", chainEntity.key)
                                put("salt", chainEntity.salt)
                                put("links", buildJsonArray {
                                    chainLinkEntities.map { chainLinkEntity ->
                                        addJsonObject {
                                            put("id", chainLinkEntity.id)
                                            put("name", chainLinkEntity.name)
                                            put("description", chainLinkEntity.description)
                                            put("password", chainLinkEntity.password)
                                            put("iv", chainLinkEntity.iv)
                                        }
                                    }
                                })
                            }
                        }
                    }

                    TcpSocket.Payload(TcpSocket.Route.CHAIN_SYNC.name, chainJsonArray)
                }
                TcpSocket.Route.CHAIN_LINK_SYNC -> {
                    val chainLinkEntities = chainLinkRepository.getBy(readPayload.data!!.jsonPrimitive.content)

                    val chainLinkJsonArray = buildJsonArray {
                        chainLinkEntities.map { chainLinkEntity ->
                            addJsonObject {
                                put("id", chainLinkEntity.id)
                                put("name", chainLinkEntity.name)
                                put("description", chainLinkEntity.description)
                                put("password", chainLinkEntity.password)
                                put("iv", chainLinkEntity.iv)
                            }
                        }
                    }

                    TcpSocket.Payload(TcpSocket.Route.CHAIN_LINK_SYNC.name, chainLinkJsonArray)
                }
            }

            writeChannel.writeStringUtf8("${Json.encodeToString(writePayload)}\n")
        }
    }

    fun stop() = try {
        serverSocket?.close()
        selectorManager?.close()
    } catch (_: Exception) {}
}