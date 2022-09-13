package io.sunland.chainpass.common.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.StorageType
import io.sunland.chainpass.common.component.DropdownMenu
import io.sunland.chainpass.common.component.DropdownMenuItem
import io.sunland.chainpass.common.component.ValidationTextField
import kotlinx.coroutines.Job

class ServerAddress {
    class Host(value: String? = null) {
        var value = value ?: ""
            private set

        val validation = value?.let {
            if (value.isEmpty()) {
                Result.failure(IllegalArgumentException("Host is empty"))
            } else if (!value.matches("^(([a-zA-Z]|[a-zA-Z][a-zA-Z\\d\\-]*[a-zA-Z\\d])\\.)*([A-Za-z]|[A-Za-z][A-Za-z\\d\\-]*[A-Za-z\\d])$".toRegex()) &&
                !value.matches("^((\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.){3}(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])$".toRegex())
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
            } else if (!value.matches("^([1-9]\\d{0,3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$".toRegex())) {
                Result.failure(IllegalArgumentException("Port is not a valid port number"))
            } else Result.success(value)
        } ?: Result.success(this.value)
    }

    var host = Host()
    var port = Port()
}

class ServerConnectionState(serverAddress: ServerAddress, storageType: StorageType) {
    val hostState = mutableStateOf(serverAddress.host.value)
    val hostValidationState = mutableStateOf(serverAddress.host.validation)

    val portState = mutableStateOf(serverAddress.port.value)
    val portValidationState = mutableStateOf(serverAddress.port.validation)

    val discoveringState = mutableStateOf<Job?>(null)

    val storageTypeState = mutableStateOf(storageType)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ServerConnection(
    serverConnectionState: ServerConnectionState,
    onDiscover: () -> Unit,
    onDiscoverCancel: () -> Unit,
    onConnect: (ServerAddress, StorageType) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

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

    val storageTypeMenuExpandedState = remember { mutableStateOf(false) }

    val onStorageTypeChange = { value: StorageType ->
        serverConnectionState.storageTypeState.value = value
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
            onConnect(serverAddress, serverConnectionState.storageTypeState.value)
        }
    }

    val onKeyEvent = { keyEvent: KeyEvent ->
        if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
            onDone()

            true
        } else false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ServerConnectionTopBar(serverConnectionState = serverConnectionState, onConnect = onDone)
        Column(
            modifier = Modifier.fillMaxWidth(fraction = 0.6f).align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(modifier = Modifier.padding(vertical = 32.dp), text = "Server Address", fontWeight = FontWeight.Bold)
            ValidationTextField(
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onKeyEvent(onKeyEvent),
                placeholder = { Text(text = "Host") },
                value = if (serverConnectionState.discoveringState.value?.isActive == true) {
                    "Discovering..."
                } else serverConnectionState.hostState.value,
                onValueChange = onHostChange,
                singleLine = true,
                leadingIcon = {
                    IconButton(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = onDiscover,
                        enabled = serverConnectionState.discoveringState.value?.isActive != true
                    ) { Icon(imageVector = Icons.Default.Refresh, contentDescription = null) }
                },
                trailingIcon = if (serverConnectionState.discoveringState.value?.isActive == true) {
                    {
                        Icon(
                            modifier = Modifier
                                .clickable(onClick = onDiscoverCancel)
                                .pointerHoverIcon(icon = PointerIconDefaults.Hand),
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null)
                    }
                } else if (serverConnectionState.hostValidationState.value.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                enabled = serverConnectionState.discoveringState.value?.isActive != true,
                isError = serverConnectionState.discoveringState.value?.isActive != true &&
                        serverConnectionState.hostValidationState.value.isFailure,
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
                modifier = Modifier.fillMaxWidth().onKeyEvent(onKeyEvent),
                placeholder = { Text(text = "Port") },
                value = if (serverConnectionState.discoveringState.value?.isActive == true) {
                    "Discovering..."
                } else serverConnectionState.portState.value,
                onValueChange = onPortChange,
                singleLine = true,
                trailingIcon = if (serverConnectionState.discoveringState.value?.isActive != true &&
                    serverConnectionState.portValidationState.value.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                enabled = serverConnectionState.discoveringState.value?.isActive != true,
                isError = serverConnectionState.discoveringState.value?.isActive != true &&
                        serverConnectionState.portValidationState.value.isFailure,
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
            Text(modifier = Modifier.padding(vertical = 32.dp), text = "Storage Type", fontWeight = FontWeight.Bold)
            Button(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = { storageTypeMenuExpandedState.value = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background)
            ) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.width(width = 60.dp).padding(start = 8.dp, end = 4.dp),
                        text = serverConnectionState.storageTypeState.value.name
                    )
                    Icon(imageVector = Icons.Default.ExpandMore, contentDescription = null)
                }
                DropdownMenu(
                    modifier = Modifier.width(width = 150.dp),
                    expanded = storageTypeMenuExpandedState.value,
                    onDismissRequest = { storageTypeMenuExpandedState.value = false },
                    offset = DpOffset(x = 8.dp, y = (-16).dp)
                ) {
                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = {
                            storageTypeMenuExpandedState.value = false

                            onStorageTypeChange(StorageType.JSON)
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { Text(text = "JSON", fontSize = 12.sp) }
                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = {
                            storageTypeMenuExpandedState.value = false

                            onStorageTypeChange(StorageType.CSV)
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { Text(text = "CSV", fontSize = 12.sp) }
                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = {
                            storageTypeMenuExpandedState.value = false

                            onStorageTypeChange(StorageType.TXT)
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { Text(text = "TXT", fontSize = 12.sp) }
                }
            }

            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        }
    }
}