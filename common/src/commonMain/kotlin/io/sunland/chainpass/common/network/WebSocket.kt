package io.sunland.chainpass.common.network

import io.ktor.utils.io.core.*
import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.metadata.RoutingMetadata
import io.rsocket.kotlin.metadata.metadata
import io.rsocket.kotlin.metadata.read
import io.rsocket.kotlin.payload.Payload
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object WebSocket {
    enum class Route(val path: String) {
        CHAIN_CREATE("chains.create"),
        CHAIN_READ("chains.read"),
        CHAIN_DELETE("chains.delete"),
        CHAIN_KEY("chains.key"),
        CHAIN_LINK_CREATE("chain.links.create"),
        CHAIN_LINK_READ("chain.links.read"),
        CHAIN_LINK_UPDATE("chain.links.update"),
        CHAIN_LINK_DELETE("chain.links.delete")
    }

    @OptIn(ExperimentalMetadataApi::class)
    fun Payload.getRoute(): Route = metadata?.read(RoutingMetadata)?.tags?.firstOrNull()?.let { path ->
        Route.values().first { route -> route.path == path }
    } ?: error("No payload route provided")

    @OptIn(ExperimentalMetadataApi::class)
    fun Payload.Companion.encode(route: Route): Payload = buildPayload {
        data(ByteReadPacket.Empty)
        metadata(RoutingMetadata(route.path))
    }

    @OptIn(ExperimentalMetadataApi::class)
    inline fun <reified T> Payload.Companion.encode(route: Route, value: T): Payload = buildPayload {
        data(Json.encodeToString(value))
        metadata(RoutingMetadata(route.path))
    }

    inline fun <reified T> Payload.Companion.encode(value: T): Payload = buildPayload {
        data(Json.encodeToString(value))
    }

    inline fun <reified T> Payload.decode(): T = Json.decodeFromString(data.readText())
}