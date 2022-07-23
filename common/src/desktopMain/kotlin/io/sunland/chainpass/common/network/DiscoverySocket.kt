package io.sunland.chainpass.common.network

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import java.net.DatagramSocket
import java.net.InetAddress

actual object DiscoverySocket {
    actual fun getLocalHost() = DatagramSocket().use { socket ->
        socket.connect(InetAddress.getByName(SocketConfig.DNS), SocketConfig.PORT)
        socket.localAddress.hostAddress!!
    }

    actual suspend fun discover() = withContext(Dispatchers.IO) {
        val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).udp().bind()

        (2..254).map { index -> "${getLocalHost().substringBeforeLast(".")}.$index" }.forEach { host ->
            if (InetAddress.getByName(host).isReachable(SocketConfig.TIMEOUT)) {
                try {
                    val serverAddress = withTimeout(SocketConfig.TIMEOUT.toLong()) {
                        socket.send(Datagram(
                            ByteReadPacket(SocketConfig.MESSAGE.toByteArray()),
                            InetSocketAddress(host, SocketConfig.PORT)
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