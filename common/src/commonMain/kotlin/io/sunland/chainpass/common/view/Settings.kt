package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
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
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.component.ValidationTextField

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
fun Settings(
    hostAddress: String,
    storePath: String,
    settingsState: SettingsState,
    navigationState: NavigationState,
    modifier: Modifier = Modifier
) {
    val deviceAddressState = remember { mutableStateOf(settingsState.deviceAddressState.value) }
    val deviceAddressErrorState = remember { mutableStateOf(false) }

    val onDeviceAddressChange = { value: String ->
        val deviceAddress = DeviceAddress(value)

        deviceAddressState.value = deviceAddress.value
        deviceAddressErrorState.value = deviceAddress.validation.isFailure
    }

    val passwordLengthState = remember { mutableStateOf(settingsState.passwordLengthState.value.toFloat()) }
    val passwordIsAlphanumericState = remember { mutableStateOf(settingsState.passwordIsAlphanumericState.value) }

    val onPasswordLengthChange = { value: Float ->
        passwordLengthState.value = value
    }

    val onPasswordIsAlphanumericChange = { value: Boolean ->
        passwordIsAlphanumericState.value = value
    }

    val onDone = {
        deviceAddressErrorState.value = DeviceAddress(deviceAddressState.value).validation.isFailure

        if (!deviceAddressErrorState.value) {
            settingsState.deviceAddressState.value = deviceAddressState.value
            settingsState.passwordLengthState.value = passwordLengthState.value.toInt()
            settingsState.passwordIsAlphanumericState.value = passwordIsAlphanumericState.value

            settingsState.save()

            navigationState.screenState.value = Screen.CHAIN_LIST
        }
    }

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) { Text(text = "Settings") }
            },
            modifier = Modifier.fillMaxWidth(),
            navigationIcon = {
                IconButton(
                    onClick = { navigationState.screenState.value = Screen.CHAIN_LIST },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
                ) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null) }
            },
            actions = {
                IconButton(
                    onClick = { onDone() },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
                ) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberScrollState(initial = 0)

            Column(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .padding(vertical = 16.dp)
                    .align(alignment = Alignment.Center)
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
                                    append(hostAddress)
                                }
                                append(" to sync with other devices.")
                            },
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val focusRequester = remember { FocusRequester() }

                        LaunchedEffect(focusRequester) {
                            focusRequester.requestFocus()
                        }

                        ValidationTextField(
                            value = deviceAddressState.value,
                            onValueChange = onDeviceAddressChange,
                            modifier = Modifier.focusRequester(focusRequester = focusRequester),
                            placeholder = { Text(text = "Device Address") },
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
                            value = passwordLengthState.value,
                            onValueChange = onPasswordLengthChange,
                            modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                            valueRange = 8f..32f
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Alphanumeric:")
                        Switch(
                            checked = passwordIsAlphanumericState.value,
                            onCheckedChange = onPasswordIsAlphanumericChange,
                            modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
                        )
                    }
                }
                Text(text = "Store", fontWeight = FontWeight.Bold)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(space = 16.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Files are stored at ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(storePath)
                            }
                            append(" directory.")
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}