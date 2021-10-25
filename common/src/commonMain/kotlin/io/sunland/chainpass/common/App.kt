package io.sunland.chainpass.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.launch

@Composable
fun App(httpClient: HttpClient) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    Scaffold(modifier = Modifier.fillMaxSize(), scaffoldState = scaffoldState) {
        Box(modifier = Modifier.fillMaxSize().padding(all = 16.dp)) {
            Button(
                modifier = Modifier.align(Alignment.Center),
                onClick = {
                    coroutineScope.launch {
                        try {
                            httpClient.webSocket(request = {
                                header("Socket-Type", WebSocket.Type.CLIENT)
                            }) {
                                send(WebSocket.Message("Chain Pass", WebSocket.Type.CLIENT).toFrame())

                                while (true) {
                                    val frame = incoming.receive() as? Frame.Text ?: continue

                                    val message = WebSocket.Message.from(frame)

                                    scaffoldState.snackbarHostState.showSnackbar(message.text)

                                    break
                                }

                                close()
                            }
                        } catch (ex: Throwable) {
                            scaffoldState.snackbarHostState.showSnackbar(ex.message!!)
                        }
                    }
                }
            ) { Text("Chain Pass") }
        }
    }
}
