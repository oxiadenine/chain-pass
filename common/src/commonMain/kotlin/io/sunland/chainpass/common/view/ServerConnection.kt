package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Settings

class ServerAddress : Settings {
    class Host(value: String? = null) {
        var value = value ?: ""
            private set

        val isValid = value?.let {
            value.isNotEmpty() &&
                    (value.matches("^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$".toRegex()) ||
                            value.matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$".toRegex()))
        } ?: true
    }

    class Port(value: String? = null) {
        var value = value ?: ""
            private set

        val isValid = value?.let {
            value.isNotEmpty() &&
                    value.matches("^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$".toRegex())
        } ?: true
    }

    enum class Protocol { WS, WSS }

    var host = Host()
    var port = Port()
    var protocol = Protocol.WS

    override val fileName: String = "server-address"

    override fun toMap(): Map<String, String> = mapOf(
        "host" to host.value,
        "port" to port.value,
        "protocol" to protocol.name
    )

    override fun fromMap(data: Map<String, String>) = apply {
        host = Host(data["host"]!!)
        port = Port(data["port"]!!)
        protocol = Protocol.valueOf(data["protocol"]!!)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ServerConnection(serverAddress: ServerAddress, onIconDoneClick: (ServerAddress) -> Unit) {
    val hostState = mutableStateOf(serverAddress.host.value)
    val hostErrorState = mutableStateOf(!serverAddress.host.isValid)

    val portState = mutableStateOf(serverAddress.port.value)
    val portErrorState = mutableStateOf(!serverAddress.port.isValid)

    val secureState = mutableStateOf(serverAddress.protocol != ServerAddress.Protocol.WS)

    val onHostChange = { host: String ->
        serverAddress.host = ServerAddress.Host(host)

        hostState.value = serverAddress.host.value
        hostErrorState.value = !serverAddress.host.isValid
    }

    val onPortChange = { port: String ->
        serverAddress.port = ServerAddress.Port(port)

        portState.value = serverAddress.port.value
        portErrorState.value = !serverAddress.port.isValid
    }

    val onSecureCheckedChange = { isSecure: Boolean ->
        serverAddress.protocol = if (!isSecure) {
            ServerAddress.Protocol.WS
        } else ServerAddress.Protocol.WSS

        secureState.value = serverAddress.protocol != ServerAddress.Protocol.WS
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier.align(Alignment.TopStart),
            title = { Text(text = "Chain Pass") },
            actions = {
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = {
                        serverAddress.host = ServerAddress.Host(hostState.value)
                        serverAddress.port = ServerAddress.Port(portState.value)
                        serverAddress.protocol = if (!secureState.value) {
                            ServerAddress.Protocol.WS
                        } else ServerAddress.Protocol.WSS

                        hostErrorState.value = !serverAddress.port.isValid
                        portErrorState.value = !serverAddress.host.isValid

                        if (!hostErrorState.value && !portErrorState.value) {
                            onIconDoneClick(serverAddress)
                        }
                    }
                ) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
            }
        )
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(modifier = Modifier.padding(vertical = 32.dp), text = "Server Address", fontWeight = FontWeight.Bold)
            TextField(
                placeholder = { Text(text = "Host") },
                value = hostState.value,
                onValueChange = onHostChange,
                singleLine = true,
                trailingIcon = if (hostErrorState.value) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = hostErrorState.value,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            TextField(
                placeholder = { Text(text = "Port") },
                value = portState.value,
                onValueChange = onPortChange,
                singleLine = true,
                trailingIcon = if (portErrorState.value) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = portErrorState.value,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                Text(text = "Secure")
                Checkbox(checked = secureState.value, onCheckedChange = onSecureCheckedChange)
            }
        }
    }
}