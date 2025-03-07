package io.github.oxiadenine.chainpass.network

import io.github.oxiadenine.chainpass.repository.ChainEntity
import io.github.oxiadenine.chainpass.repository.ChainLinkEntity
import io.github.oxiadenine.chainpass.repository.ChainLinkRepository
import io.github.oxiadenine.chainpass.repository.ChainRepository
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

class ChainApi(
    private val chainRepository: ChainRepository,
    private val chainLinkRepository: ChainLinkRepository,
    private val syncClient: SyncClient
) {
    suspend fun sync() = runCatching {
        val socket = syncClient.connect()

        val readChannel = socket.openReadChannel()
        val writeChannel = socket.openWriteChannel(autoFlush = true)

        val writePayload = TcpSocket.Payload(TcpSocket.Route.CHAIN_SYNC.name)

        writeChannel.writeStringUtf8("${Json.encodeToString(writePayload)}\n")

        val readPayload = Json.decodeFromString<TcpSocket.Payload>(readChannel.readUTF8Line()!!)

        readPayload.data!!.jsonArray.forEach { chainJsonElement ->
            val chainJsonObject = chainJsonElement.jsonObject

            val chainEntity = ChainEntity(
                chainJsonObject["id"]!!.jsonPrimitive.content,
                chainJsonObject["name"]!!.jsonPrimitive.content,
                chainJsonObject["key"]!!.jsonPrimitive.content,
                chainJsonObject["salt"]!!.jsonPrimitive.content
            )

            chainRepository.getOne(chainEntity.id).onFailure {
                chainRepository.create(chainEntity)
            }

            val chainLinkEntities = chainJsonObject["links"]!!.jsonArray.map { chainLinkJsonElement ->
                val chainLinkJsonObject = chainLinkJsonElement.jsonObject

                ChainLinkEntity(
                    chainLinkJsonObject["id"]!!.jsonPrimitive.content,
                    chainLinkJsonObject["name"]!!.jsonPrimitive.content,
                    chainLinkJsonObject["description"]!!.jsonPrimitive.content,
                    chainLinkJsonObject["password"]!!.jsonPrimitive.content,
                    chainLinkJsonObject["iv"]!!.jsonPrimitive.content,
                    chainEntity.id
                )
            }

            chainLinkEntities.forEach { chainLinkEntity ->
                chainLinkRepository.getOne(chainLinkEntity.id).onSuccess { chainLinkEntityFound ->
                    if (chainLinkEntity.password != chainLinkEntityFound.password
                        || chainLinkEntity.description != chainLinkEntityFound.description) {
                        chainLinkRepository.update(chainLinkEntity)
                    }
                }.onFailure {
                    chainLinkRepository.create(chainLinkEntity)
                }
            }
        }
    }
}