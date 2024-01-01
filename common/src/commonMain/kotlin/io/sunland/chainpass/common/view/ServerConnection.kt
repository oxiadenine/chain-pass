package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Settings
import io.sunland.chainpass.common.component.ValidationTextField

class ServerAddress : Settings {
    class Host(value: String? = null) {
        var value = value ?: ""
            private set

        val validation = value?.let {
            if (value.isEmpty()) {
                Result.failure(IllegalArgumentException("Host is empty"))
            } else if (!value.matches("^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$".toRegex()) &&
                !value.matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$".toRegex())
            ) {
                Result.failure(IllegalArgumentException("Host is not a valid hostname or IP address"))
            } else Result.success(value)
        } ?: Result.success(this.value)
    }

    class Port(value: String? = null) {
        var value = value ?: ""
            private set

        val validation = value?.let {
            if (value.isEmpty()) {
                Result.failure(IllegalArgumentException("Port is empty"))
            } else if (!value.matches("^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$".toRegex())) {
                Result.failure(IllegalArgumentException("Port is not a valid port number"))
            } else Result.success(value)
        } ?: Result.success(this.value)
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
    val hostValidationState = mutableStateOf(serverAddress.host.validation)

    val portState = mutableStateOf(serverAddress.port.value)
    val portValidationState = mutableStateOf(serverAddress.port.validation)

    val secureState = mutableStateOf(serverAddress.protocol != ServerAddress.Protocol.WS)

    val onHostChange = { host: String ->
        serverAddress.host = ServerAddress.Host(host)

        hostState.value = serverAddress.host.value
        hostValidationState.value = serverAddress.host.validation
    }

    val onPortChange = { port: String ->
        serverAddress.port = ServerAddress.Port(port)

        portState.value = serverAddress.port.value
        portValidationState.value = serverAddress.port.validation
    }

    val onSecureCheckedChange = { isSecure: Boolean ->
        serverAddress.protocol = if (!isSecure) {
            ServerAddress.Protocol.WS
        } else ServerAddress.Protocol.WSS

        secureState.value = serverAddress.protocol != ServerAddress.Protocol.WS
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
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

                        hostValidationState.value = serverAddress.host.validation
                        portValidationState.value = serverAddress.port.validation

                        if (hostValidationState.value.isSuccess && portValidationState.value.isSuccess) {
                            onIconDoneClick(serverAddress)
                        }
                    }
                ) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
            }
        )
        Column(
            modifier = Modifier.fillMaxWidth(fraction = 0.6f).align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val focusRequester = FocusRequester()

            Text(modifier = Modifier.padding(vertical = 32.dp), text = "Server Address", fontWeight = FontWeight.Bold)
            ValidationTextField(
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                placeholder = { Text(text = "Host") },
                value = hostState.value,
                onValueChange = onHostChange,
                singleLine = true,
                isError = hostValidationState.value.isFailure,
                errorMessage = hostValidationState.value.exceptionOrNull()?.message,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            ValidationTextField(
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Port") },
                value = portState.value,
                onValueChange = onPortChange,
                singleLine = true,
                trailingIcon = if (portValidationState.value.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = portValidationState.value.isFailure,
                errorMessage = portValidationState.value.exceptionOrNull()?.message,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                Text(text = "Secure")
                Checkbox(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    checked = secureState.value,
                    onCheckedChange = onSecureCheckedChange
                )
            }

            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        }
    }
}