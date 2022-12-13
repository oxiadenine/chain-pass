package io.sunland.chainpass.common.network

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.text.toByteArray

actual fun DiscoverySocket.getLocalHost() = DatagramSocket().use { socket ->
    socket.connect(InetAddress.getByName(DNS), PORT)
    socket.localAddress.hostAddress!!
}

actual suspend fun DiscoverySocket.discover() = withContext(Dispatchers.IO) {
    val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).udp().bind()

    (2..254).map { index -> "${getLocalHost().substringBeforeLast(".")}.$index" }.forEach { host ->
        if (InetAddress.getByName(host).isReachable(TIMEOUT)) {
            try {
                val serverAddress = withTimeout(TIMEOUT.toLong()) {
                    socket.send(Datagram(
                        ByteReadPacket(MESSAGE.toByteArray()),
                        InetSocketAddress(host, PORT)
                    ))

                    return@withTimeout socket.receive().packet.readText()
                }

                socket.close()

                return@withContext serverAddress
            } catch (_: TimeoutCancellationException) {}
        }
    }

    socket.close()

    return@withContext ""
}