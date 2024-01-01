package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.component.Dialog
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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(settingsState: SettingsState, onClose: () -> Unit, storePath: String) {
    var deviceAddress by remember { mutableStateOf(DeviceAddress(settingsState.deviceAddressState.value)) }

    val onDeviceAddressTextFieldValueChange = { address: String ->
        deviceAddress = DeviceAddress(address)
    }

    var passwordLength by remember { mutableStateOf(settingsState.passwordLengthState.value.toFloat()) }
    val passwordLengthValueRange by remember { mutableStateOf(8f..32f) }

    var passwordIsAlphanumeric by remember { mutableStateOf(settingsState.passwordIsAlphanumericState.value) }

    val onPasswordLengthChange = { length: Float ->
        if (length >= passwordLengthValueRange.start && length <= passwordLengthValueRange.endInclusive) {
            passwordLength = length
        }
    }

    val onPasswordIsAlphanumericChange = { isAlphanumeric: Boolean ->
        passwordIsAlphanumeric = isAlphanumeric
    }

    val onInputDialogConfirmRequest = {
        deviceAddress = DeviceAddress(deviceAddress.value)

        if (deviceAddress.validation.isSuccess) {
            settingsState.deviceAddressState.value = deviceAddress.value
            settingsState.passwordLengthState.value = passwordLength.toInt()
            settingsState.passwordIsAlphanumericState.value = passwordIsAlphanumeric

            settingsState.save()

            onClose()
        }
    }

    Dialog(
        onDismissRequest = onClose,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Settings")
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
                ) { Icon(imageVector = Icons.Default.Close, contentDescription = null) }
            }
        },
        buttons = {
            TextButton(
                onClick = onInputDialogConfirmRequest,
                modifier = Modifier.align(alignment = Alignment.End).pointerHoverIcon(icon = PointerIconDefaults.Hand)
            ) { Text(text = "Save") }
        }
    ) {
        val scrollState = rememberScrollState(initial = 0)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).verticalScroll(state = scrollState),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
                Text(text = "Sync", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val focusRequester = remember { FocusRequester() }

                    ValidationTextField(
                        value = deviceAddress.value,
                        onValueChange = onDeviceAddressTextFieldValueChange,
                        modifier = Modifier
                            .focusRequester(focusRequester = focusRequester)
                            .onKeyEvent { keyEvent: KeyEvent ->
                                if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                                    onInputDialogConfirmRequest()

                                    true
                                } else false
                            }
                        ,
                        placeholder = { Text(text = "Device Address", fontSize = 14.sp) },
                        singleLine = true,
                        leadingIcon = { Icon(imageVector = Icons.Default.Devices, contentDescription = null) },
                        trailingIcon = if (deviceAddress.validation.isFailure) {
                            { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                        } else null,
                        isError = deviceAddress.validation.isFailure,
                        errorMessage = deviceAddress.validation.exceptionOrNull()?.message,
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        ),
                        keyboardActions = KeyboardActions(onDone = { onInputDialogConfirmRequest() })
                    )

                    LaunchedEffect(focusRequester) {
                        focusRequester.requestFocus()
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
                Text(text = "Password", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Length")
                        Slider(
                            value = passwordLength,
                            onValueChange = onPasswordLengthChange,
                            modifier = Modifier
                                .weight(1f, false)
                                .pointerHoverIcon(icon = PointerIconDefaults.Hand)
                                .onKeyEvent { keyEvent: KeyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyUp) {
                                        if (keyEvent.key == Key.DirectionLeft) {
                                            onPasswordLengthChange(passwordLength - 1)
                                        } else if (keyEvent.key == Key.DirectionRight) {
                                            onPasswordLengthChange(passwordLength + 1)
                                        }

                                        true
                                    } else false
                                }
                            ,
                            valueRange = passwordLengthValueRange
                        )
                        Text(text = passwordLength.toInt().toString())
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Alphanumeric")
                        Switch(
                            checked = passwordIsAlphanumeric,
                            onCheckedChange = onPasswordIsAlphanumericChange,
                            modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
                Text(text = "Store", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = buildAnnotatedString {
                        append("Files are stored at ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(storePath)
                        }
                        append(" directory.")
                    }
                )
            }
        }
    }
}