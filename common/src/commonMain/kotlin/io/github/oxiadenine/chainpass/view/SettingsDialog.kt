package io.github.oxiadenine.chainpass.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.oxiadenine.chainpass.Intl
import io.github.oxiadenine.chainpass.LocalIntl
import io.github.oxiadenine.chainpass.SettingsState
import io.github.oxiadenine.chainpass.component.Dialog
import io.github.oxiadenine.chainpass.component.ValidationTextField

class DeviceAddress(value: String? = null) {
    object InvalidIPv4Error : Error() {
        private fun readResolve(): Any = InvalidIPv4Error
    }

    var value = value ?: ""
        private set

    val validation = value?.let {
        if (value.isNotEmpty() && !value.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$".toRegex())) {
            Result.failure(InvalidIPv4Error)
        } else Result.success(value)
    } ?: Result.success(this.value)
}

@Composable
fun SettingsDialog(settingsState: SettingsState, onClose: () -> Unit, storeDirPath: String) {
    val intl = LocalIntl.current

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

    var selectedLanguage by remember {
        mutableStateOf(Intl.languages.first { language -> language == settingsState.languageState.value })
    }

    val onLanguageListItemClick = { language: String ->
        selectedLanguage = language
    }

    val onInputDialogConfirmRequest = {
        deviceAddress = DeviceAddress(deviceAddress.value)

        if (deviceAddress.validation.isSuccess) {
            settingsState.deviceAddressState.value = deviceAddress.value
            settingsState.passwordLengthState.value = passwordLength.toInt()
            settingsState.passwordIsAlphanumericState.value = passwordIsAlphanumeric
            settingsState.languageState.value = selectedLanguage

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
                Text(text = intl.translate("dialog.settings.title"))
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                ) { Icon(imageVector = Icons.Default.Close, contentDescription = null) }
            }
        },
        buttons = {
            TextButton(
                onClick = onInputDialogConfirmRequest,
                modifier = Modifier.align(alignment = Alignment.End).pointerHoverIcon(icon = PointerIcon.Hand)
            ) { Text(text = intl.translate("dialog.settings.button.save.text")) }
        }
    ) {
        val scrollState = rememberScrollState(initial = 0)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).verticalScroll(state = scrollState),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
                Text(
                    text = intl.translate("dialog.settings.item.sync.title"),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
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
                        placeholder = {
                            Text(
                                text = intl.translate("dialog.settings.item.sync.textField.device.placeholder"),
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        leadingIcon = { Icon(imageVector = Icons.Default.Devices, contentDescription = null) },
                        trailingIcon = if (deviceAddress.validation.isFailure) {
                            { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                        } else null,
                        isError = deviceAddress.validation.isFailure,
                        errorMessage = deviceAddress.validation.exceptionOrNull()?.let { error ->
                            if (error is DeviceAddress.InvalidIPv4Error) {
                                intl.translate("dialog.settings.item.sync.textFiled.device.error")
                            } else null
                        },
                        colors = TextFieldDefaults.colors(
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
                Text(
                    text = intl.translate("dialog.settings.item.password.title"),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = intl.translate("dialog.settings.item.password.length.text"))
                        Slider(
                            value = passwordLength,
                            onValueChange = onPasswordLengthChange,
                            modifier = Modifier
                                .weight(1f, false)
                                .pointerHoverIcon(icon = PointerIcon.Hand)
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
                        Text(text = intl.translate("dialog.settings.item.password.alphanumeric.text"))
                        Switch(
                            checked = passwordIsAlphanumeric,
                            onCheckedChange = onPasswordIsAlphanumericChange,
                            modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
                Text(
                    text = intl.translate("dialog.settings.item.storage.title"),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                )
                Text(
                    text = buildAnnotatedString {
                        append(intl.translate("dialog.settings.item.storage.text"))
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(storeDirPath)
                        }
                    }
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
                Text(
                    text = intl.translate("dialog.settings.item.language.title"),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    Intl.languages.forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLanguageListItemClick(language) }
                                .padding(all = 8.dp)
                                .pointerHoverIcon(icon = PointerIcon.Hand),
                            horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedLanguage == language) {
                                Icon(imageVector = Icons.Default.RadioButtonChecked, contentDescription = null)
                            } else Icon(imageVector = Icons.Default.RadioButtonUnchecked, contentDescription = null)
                            Column {
                                Text(text = intl.translate("dialog.settings.item.language.item.$language.text"))
                                Text(
                                    text = language,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}