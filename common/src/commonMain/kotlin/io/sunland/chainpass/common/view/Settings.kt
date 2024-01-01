package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Settings
import io.sunland.chainpass.common.component.ValidationTextField
import io.sunland.chainpass.common.component.VerticalScrollbar

class DeviceAddress(value: String? = null) {
    var value = value ?: ""
        private set

    val validation = value?.let {
        if (value.isNotEmpty() && !value.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$".toRegex())) {
            Result.failure(IllegalArgumentException("Device Address is not a valid IPv4 address"))
        } else Result.success(value)
    } ?: Result.success(this.value)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Settings(settings: Settings, onBack: (Settings) -> Unit) {
    val focusRequester = remember { FocusRequester() }

    val scrollState = rememberScrollState()

    val isDeviceAddressEditState = remember { mutableStateOf(false) }

    val deviceAddressState = remember { mutableStateOf(settings.deviceAddress) }
    val deviceAddressErrorState = remember { mutableStateOf(false) }

    val onDeviceAddressChange = { value: String ->
        val deviceAddress = DeviceAddress(value)

        deviceAddressState.value = deviceAddress.value
        deviceAddressErrorState.value = deviceAddress.validation.isFailure
    }

    val passwordLengthState = remember { mutableStateOf(settings.passwordLength.toFloat()) }
    val passwordIsAlphanumericState = remember { mutableStateOf(settings.passwordIsAlphanumeric) }

    val onPasswordLengthChange = { value: Float ->
        passwordLengthState.value = value
    }

    val onPasswordIsAlphanumericChange = { value: Boolean ->
        passwordIsAlphanumericState.value = value
    }

    val onDone = {
        deviceAddressErrorState.value = DeviceAddress(deviceAddressState.value).validation.isFailure

        if (deviceAddressErrorState.value) {
            deviceAddressState.value = settings.deviceAddress
            deviceAddressErrorState.value = false
        }

        isDeviceAddressEditState.value = false

        onBack(Settings(
            hostAddress = settings.hostAddress,
            deviceAddress = deviceAddressState.value,
            passwordLength = passwordLengthState.value.toInt(),
            passwordIsAlphanumeric = passwordIsAlphanumericState.value
        ))
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
                    onClick = { onDone() }
                ) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null) }
            }
        )
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .align(Alignment.Center)
                    .verticalScroll(state = scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = 32.dp)
            ) {
                Text(text = "Sync", fontWeight = FontWeight.Bold)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(space = 16.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = buildAnnotatedString {
                                append("Your IPv4 address ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                    append(settings.hostAddress)
                                }
                                append(" to sync with other devices.")
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                    if (isDeviceAddressEditState.value) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ValidationTextField(
                                modifier = Modifier.focusRequester(focusRequester),
                                placeholder = { Text(text = "Device Address") },
                                value = deviceAddressState.value,
                                onValueChange = onDeviceAddressChange,
                                singleLine = true,
                                trailingIcon = if (deviceAddressErrorState.value) {
                                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                                } else null,
                                isError = deviceAddressErrorState.value,
                                errorMessage = DeviceAddress(deviceAddressState.value).validation.exceptionOrNull()?.message,
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent
                                ),
                                keyboardActions = KeyboardActions(onDone = { onDone() })
                            )
                        }

                        LaunchedEffect(isDeviceAddressEditState.value) { focusRequester.requestFocus() }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (settings.deviceAddress.isEmpty()) {
                                Text(text = "Device Address", fontWeight = FontWeight.ExtraLight)
                            } else Text(text = deviceAddressState.value)

                            IconButton(
                                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                                onClick = { isDeviceAddressEditState.value = true }
                            ) { Icon(imageVector = Icons.Default.Edit, contentDescription = null) }
                        }
                    }
                }
                Text(text = "Password", fontWeight = FontWeight.Bold)
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(space = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Length:")
                        Text(text = passwordLengthState.value.toInt().toString())
                        Slider(
                            modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                            value = passwordLengthState.value,
                            onValueChange = onPasswordLengthChange,
                            valueRange = 8f..32f
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Alphanumeric:")
                        Switch(
                            modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                            checked = passwordIsAlphanumericState.value,
                            onCheckedChange = onPasswordIsAlphanumericChange
                        )
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                scrollState = scrollState
            )
        }
    }
}