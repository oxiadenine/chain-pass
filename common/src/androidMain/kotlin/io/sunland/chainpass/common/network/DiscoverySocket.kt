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

actual object DiscoverySocketClient {
    actual suspend fun discover() = withContext(Dispatchers.IO) {
        val localhost = DatagramSocket().use { socket ->
            socket.connect(InetAddress.getByName(DiscoverySocket.DNS), DiscoverySocket.PORT)
            socket.localAddress.hostAddress!!
        }

        val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).udp().bind()

        (2..254).map { index -> "${localhost.substringBeforeLast(".")}.$index" }.forEach { host ->
            if (InetAddress.getByName(host).isReachable(DiscoverySocket.TIMEOUT)) {
                try {
                    val serverAddress = withTimeout(DiscoverySocket.TIMEOUT.toLong()) {
                        socket.send(Datagram(
                            ByteReadPacket(DiscoverySocket.MESSAGE.toByteArray()),
                            InetSocketAddress(host, DiscoverySocket.PORT)
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
}