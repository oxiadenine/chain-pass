package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
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
import kotlinx.coroutines.Job

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

    var host = Host()
    var port = Port()

    override val fileName: String = "server-address"

    override fun toMap(): Map<String, String> = mapOf("host" to host.value, "port" to port.value)

    override fun fromMap(data: Map<String, String>) = apply {
        host = Host(data["host"]!!)
        port = Port(data["port"]!!)
    }
}

class ServerConnectionState(serverAddress: ServerAddress) {
    val hostState = mutableStateOf(serverAddress.host.value)
    val hostValidationState = mutableStateOf(serverAddress.host.validation)

    val portState = mutableStateOf(serverAddress.port.value)
    val portValidationState = mutableStateOf(serverAddress.port.validation)

    val discoveringState = mutableStateOf<Job?>(null)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ServerConnection(
    serverConnectionState: ServerConnectionState,
    onIconRefreshClick: () -> Unit,
    onIconDoneClick: (ServerAddress) -> Unit
) {
    val onHostChange = { value: String ->
        val host = ServerAddress.Host(value)

        serverConnectionState.hostState.value = host.value
        serverConnectionState.hostValidationState.value = host.validation
    }

    val onPortChange = { value: String ->
        val port = ServerAddress.Port(value)

        serverConnectionState.portState.value = port.value
        serverConnectionState.portValidationState.value = port.validation
    }

    val onDone = {
        val serverAddress = ServerAddress().apply {
            host = ServerAddress.Host(serverConnectionState.hostState.value)
            port = ServerAddress.Port(serverConnectionState.portState.value)
        }

        serverConnectionState.hostValidationState.value = serverAddress.host.validation
        serverConnectionState.portValidationState.value = serverAddress.port.validation

        if (serverConnectionState.hostValidationState.value.isSuccess &&
            serverConnectionState.portValidationState.value.isSuccess) {
            onIconDoneClick(serverAddress)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
            title = { Text(text = "Chain Pass") },
            actions = {
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = onIconRefreshClick,
                    enabled = serverConnectionState.discoveringState.value?.isActive != true
                ) { Icon(imageVector = Icons.Default.Refresh, contentDescription = null) }
                if (serverConnectionState.discoveringState.value?.isActive == true) {
                    IconButton(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = {
                            serverConnectionState.discoveringState.value?.cancel()
                            serverConnectionState.discoveringState.value = null
                        },
                        enabled = serverConnectionState.discoveringState.value?.isActive == true
                    ) { Icon(imageVector = Icons.Default.Close, contentDescription = null) }
                } else {
                    IconButton(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = onDone,
                        enabled = serverConnectionState.discoveringState.value?.isActive != true
                    ) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
                }
            }
        )
        Column(
            modifier = Modifier.fillMaxWidth(fraction = 0.6f).align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val focusRequester = FocusRequester()

            Text(modifier = Modifier.padding(vertical = 32.dp), text = "Server Address", fontWeight = FontWeight.Bold)
            if (serverConnectionState.discoveringState.value?.isActive == true) {
                TextField(
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    placeholder = { Text(text = "Discovering...") },
                    enabled = false,
                    value = "",
                    onValueChange = {},
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    )
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Discovering...") },
                    enabled = false,
                    value = "",
                    onValueChange = {},
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    )
                )
            } else {
                ValidationTextField(
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    placeholder = { Text(text = "Host") },
                    value = serverConnectionState.hostState.value,
                    onValueChange = onHostChange,
                    singleLine = true,
                    isError = serverConnectionState.hostValidationState.value.isFailure,
                    errorMessage = serverConnectionState.hostValidationState.value.exceptionOrNull()?.message,
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
                    value = serverConnectionState.portState.value,
                    onValueChange = onPortChange,
                    singleLine = true,
                    trailingIcon = if (serverConnectionState.portValidationState.value.isFailure) {
                        { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                    } else null,
                    isError = serverConnectionState.portValidationState.value.isFailure,
                    errorMessage = serverConnectionState.portValidationState.value.exceptionOrNull()?.message,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    keyboardActions = KeyboardActions(onDone = { onDone() })
                )
            }

            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        }
    }
}