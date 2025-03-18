package io.github.oxiadenine.chainpass.network

import io.github.oxiadenine.chainpass.repository.ChainLinkEntity
import io.github.oxiadenine.chainpass.repository.ChainLinkRepository
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.*

class ChainLinkApi(
    private val chainLinkRepository: ChainLinkRepository,
    private val syncClient: SyncClient
) {
    suspend fun sync(chainId: String) = runCatching {
        val socket = syncClient.connect()

        val readChannel = socket.openReadChannel()
        val writeChannel = socket.openWriteChannel(autoFlush = true)

        val writePayload = TcpSocket.Payload(TcpSocket.Route.CHAIN_LINK_SYNC.name, JsonPrimitive(chainId))

        writeChannel.writeStringUtf8("${Json.encodeToString(writePayload)}\n")

        val readPayload = Json.decodeFromString<TcpSocket.Payload>(readChannel.readUTF8Line()!!)

        val chainLinkEntities = readPayload.data!!.jsonArray.map { chainLinkJsonElement ->
            val chainLinkJsonObject = chainLinkJsonElement.jsonObject

            ChainLinkEntity(
                chainLinkJsonObject["id"]!!.jsonPrimitive.content,
                chainLinkJsonObject["name"]!!.jsonPrimitive.content,
                chainLinkJsonObject["description"]!!.jsonPrimitive.content,
                chainLinkJsonObject["password"]!!.jsonPrimitive.content,
                chainLinkJsonObject["iv"]!!.jsonPrimitive.content,
                chainId
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

        socket.close()
    }
}