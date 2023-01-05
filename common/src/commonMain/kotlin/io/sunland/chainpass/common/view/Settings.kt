package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Settings
import io.sunland.chainpass.common.component.ValidationTextField
import io.sunland.chainpass.common.component.VerticalScrollbar

class DeviceIp(value: String? = null) {
    var value = value ?: ""
        private set

    val validation = value?.let {
        if (value.isNotEmpty() && !value.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$".toRegex())) {
            Result.failure(IllegalArgumentException("Device IP is not a valid IPv4 address"))
        } else Result.success(value)
    } ?: Result.success(this.value)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Settings(settings: Settings, onSave: (Settings) -> Unit, onBack: () -> Unit) {
    val focusRequester = remember { FocusRequester() }

    val scrollState = rememberScrollState()

    val deviceIpState = remember { mutableStateOf(settings.deviceIp) }
    val deviceIpErrorState = remember { mutableStateOf(false) }

    val onDeviceIpChange = { value: String ->
        val deviceIp = DeviceIp(value)

        deviceIpState.value = deviceIp.value
        deviceIpErrorState.value = deviceIp.validation.isFailure
    }

    val onDone = {
        deviceIpErrorState.value = DeviceIp(deviceIpState.value).validation.isFailure

        if (!deviceIpErrorState.value) {
            onSave(Settings(deviceIpState.value))
        }
    }

    val onKeyEvent = { keyEvent: KeyEvent ->
        if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
            onDone()

            true
        } else false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) { Text(text = "Settings") }
            },
            navigationIcon = {
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = { onBack() }
                ) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null) }
            },
            actions = {
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = { onDone() }
                ) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
            }
        )
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.6f)
                    .align(Alignment.Center)
                    .verticalScroll(state = scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = 32.dp)
            ) {
                Text(text = "Sync", fontWeight = FontWeight.Bold)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    ValidationTextField(
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onKeyEvent(onKeyEvent),
                        placeholder = { Text(text = "Device IP") },
                        value = deviceIpState.value,
                        onValueChange = onDeviceIpChange,
                        singleLine = true,
                        trailingIcon = if (deviceIpErrorState.value) {
                            { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                        } else null,
                        isError = deviceIpErrorState.value,
                        errorMessage = DeviceIp(deviceIpState.value).validation.exceptionOrNull()?.message,
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        ),
                        keyboardActions = KeyboardActions(onDone = { onDone() })
                    )
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                scrollState = scrollState
            )
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}