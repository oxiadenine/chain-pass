package io.sunland.chainpass.common.network

import androidx.compose.runtime.mutableStateOf
import io.ktor.server.cio.*
import io.rsocket.kotlin.RSocketRequestHandler
import io.rsocket.kotlin.payload.Payload
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.repository.ChainRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SyncServer(
    private val chainRepository: ChainRepository,
    private val chainLinkRepository: ChainLinkRepository
) {
    val hostAddressFlow = flow {
        while (true) {
            delay(2000)

            emit(WebSocket.getLocalHost().getOrElse { "" })
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged().conflate()

    private val engineState = mutableStateOf<CIOApplicationEngine?>(null)

    fun start(): SyncServer {
        CoroutineScope(Dispatchers.IO).launch {
            hostAddressFlow.collect { hostAddress ->
                if (hostAddress.isNotEmpty()) {
                    try {
                        engineState.value = WebSocket.start(hostAddress) {
                            RSocketRequestHandler {
                                requestResponse { payload ->
                                    when (payload.getRoute()) {
                                        WebSocket.Route.CHAIN_SYNC -> {
                                            Payload.encode(chainRepository.getAll())
                                        }
                                        WebSocket.Route.CHAIN_LINK_SYNC -> {
                                            Payload.encode(chainLinkRepository.getBy(payload.decode()))
                                        }
                                    }
                                }
                            }
                        }
                    } catch (_: Throwable) {}
                } else {
                    engineState.value?.application?.cancel()
                    engineState.value?.application?.dispose()
                }
            }
        }

        return this
    }
}