package io.github.oxiadenine.chainpass.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
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
import io.github.oxiadenine.chainpass.SettingsState
import io.github.oxiadenine.chainpass.component.Dialog
import io.github.oxiadenine.chainpass.component.ValidationTextField
import io.github.oxiadenine.composeapp.generated.resources.*
import io.github.oxiadenine.composeapp.generated.resources.Res
import io.github.oxiadenine.composeapp.generated.resources.dialog_settings_button_save_text
import io.github.oxiadenine.composeapp.generated.resources.dialog_settings_item_sync_title
import io.github.oxiadenine.composeapp.generated.resources.dialog_settings_title
import org.jetbrains.compose.resources.stringResource
import java.util.*

class DeviceAddress(value: String? = null) {
    object InvalidIPv4Error : Error() {
        private fun readResolve(): Any = InvalidIPv4Error
    }

    var value = value ?: ""
        private set

    val validation = value?.let {
        if (value.isNotEmpty() && !value.matches(
                "^(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\$".toRegex()
            )) {
            Result.failure(InvalidIPv4Error)
        } else Result.success(value)
    } ?: Result.success(this.value)
}

data class PasswordConfig(
    val length: Float = 16f,
    val lengthRange: ClosedFloatingPointRange<Float> = 8f..32f,
    val isAlphanumeric: Boolean = false
)

class SettingsDialogState(
    deviceAddress: DeviceAddress,
    passwordLength: Int,
    passwordIsAlphanumeric: Boolean,
    language: String
) {
    val deviceAddressState = mutableStateOf(deviceAddress)
    val passwordConfigState = mutableStateOf(PasswordConfig(
        length = passwordLength.toFloat(),
        isAlphanumeric = passwordIsAlphanumeric
    ))
    val selectedLocaleState = mutableStateOf(Locale(language))

    companion object {
        val Saver = listSaver(
            save = { state ->
                listOf(
                    state.deviceAddressState.value.value,
                    state.deviceAddressState.value.validation.isFailure,
                    state.passwordConfigState.value.length.toInt(),
                    state.passwordConfigState.value.isAlphanumeric,
                    state.selectedLocaleState.value.language
                )
            },
            restore = {
                val deviceAddress = it[0] as String
                val deviceAddressError = it[1] as Boolean

                SettingsDialogState(
                    if (deviceAddress.isNotEmpty() || deviceAddressError) {
                        DeviceAddress(deviceAddress)
                    } else DeviceAddress(),
                    it[2] as Int,
                    it[3] as Boolean,
                    it[4] as String
                )
            }
        )
    }
}

@Composable
fun rememberSettingsDialogState(settingsState: SettingsState) = rememberSaveable(saver = SettingsDialogState.Saver) {
    SettingsDialogState(
        DeviceAddress(settingsState.deviceAddress),
        settingsState.passwordLength,
        settingsState.passwordIsAlphanumeric,
        settingsState.language
    )
}

@Composable
fun SettingsDialog(
    settingsState: SettingsState,
    onSave: (SettingsState) -> Unit,
    onClose: () -> Unit,
    storeDirPath: String,
    languages: List<String>
) {
    val state = rememberSettingsDialogState(settingsState)

    val onDeviceAddressTextFieldValueChange = { address: String ->
        state.deviceAddressState.value = DeviceAddress(address)
    }

    val onPasswordLengthSliderValueChange = { length: Float ->
        val passwordLengthRange = state.passwordConfigState.value.lengthRange

        if (length >= passwordLengthRange.start && length <= passwordLengthRange.endInclusive) {
            state.passwordConfigState.value = state.passwordConfigState.value.copy(length = length)
        }
    }

    val onPasswordAlphanumericSwitchCheckedChange = { isAlphanumeric: Boolean ->
        state.passwordConfigState.value = state.passwordConfigState.value.copy(isAlphanumeric = isAlphanumeric)
    }

    val onLocaleListItemClick = { language: String ->
        state.selectedLocaleState.value = Locale(language)
    }

    val onInputDialogConfirmRequest = {
        val isValid = DeviceAddress(state.deviceAddressState.value.value).validation.isSuccess

        if (isValid) {
            onSave(SettingsState(
                state.deviceAddressState.value.value,
                state.passwordConfigState.value.length.toInt(),
                state.passwordConfigState.value.isAlphanumeric,
                state.selectedLocaleState.value.language
            ))
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
                Text(text = stringResource(Res.string.dialog_settings_title))
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                ) { Icon(imageVector = Icons.Default.Close, contentDescription = null) }
            }
        },
        buttons = {
            TextButton(
                onClick = onInputDialogConfirmRequest,
                modifier = Modifier
                    .align(alignment = Alignment.End)
                    .pointerHoverIcon(icon = PointerIcon.Hand)
            ) { Text(text = stringResource(Res.string.dialog_settings_button_save_text)) }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
                Text(
                    text = stringResource(Res.string.dialog_settings_item_sync_title),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val focusRequester = remember { FocusRequester() }

                    ValidationTextField(
                        value = state.deviceAddressState.value.value,
                        onValueChange = onDeviceAddressTextFieldValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onKeyEvent { keyEvent: KeyEvent ->
                                if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                                    onInputDialogConfirmRequest()

                                    true
                                } else false
                            }.focusRequester(focusRequester),
                        placeholder = {
                            Text(text = stringResource(
                                Res.string.dialog_settings_item_sync_textField_device_placeholder
                            ), fontSize = 14.sp)
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Devices, contentDescription = null)
                        },
                        trailingIcon = if (state.deviceAddressState.value.validation.isFailure) {
                            { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                        } else null,
                        isError = state.deviceAddressState.value.validation.isFailure,
                        errorMessage = state.deviceAddressState.value.validation.exceptionOrNull()?.let { error ->
                            if (error is DeviceAddress.InvalidIPv4Error) {
                                stringResource(Res.string.dialog_settings_item_sync_textFiled_device_error)
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
                    text = stringResource(Res.string.dialog_settings_item_password_title),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
                Column(modifier = Modifier.focusGroup()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(Res.string.dialog_settings_item_password_length_text))
                        Slider(
                            value = state.passwordConfigState.value.length,
                            onValueChange = onPasswordLengthSliderValueChange,
                            modifier = Modifier
                                .weight(1f, false)
                                .pointerHoverIcon(icon = PointerIcon.Hand)
                                .onKeyEvent { keyEvent: KeyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyUp) {
                                        if (keyEvent.key == Key.DirectionLeft) {
                                            onPasswordLengthSliderValueChange(state.passwordConfigState.value.length - 1f)
                                        } else if (keyEvent.key == Key.DirectionRight) {
                                            onPasswordLengthSliderValueChange(state.passwordConfigState.value.length + 1f)
                                        }

                                        true
                                    } else false
                                },
                            valueRange = state.passwordConfigState.value.lengthRange
                        )
                        Text(text = state.passwordConfigState.value.length.toInt().toString())
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(Res.string.dialog_settings_item_password_alphanumeric_text))
                        Switch(
                            checked = state.passwordConfigState.value.isAlphanumeric,
                            onCheckedChange = onPasswordAlphanumericSwitchCheckedChange,
                            modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
                Text(
                    text = stringResource(Res.string.dialog_settings_item_storage_title),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                )
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(Res.string.dialog_settings_item_storage_text))
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(storeDirPath)
                        }
                    }
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
                Text(
                    text = stringResource(Res.string.dialog_settings_item_language_title),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    languages.forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLocaleListItemClick(language) }
                                .padding(all = 8.dp)
                                .pointerHoverIcon(icon = PointerIcon.Hand),
                            horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (language == state.selectedLocaleState.value.language) {
                                Icon(imageVector = Icons.Default.RadioButtonChecked, contentDescription = null)
                            } else {
                                Icon(imageVector = Icons.Default.RadioButtonUnchecked, contentDescription = null)
                            }
                            Column {
                                Text(text = when (language) {
                                    Intl.SPANISH -> stringResource(Res.string.dialog_settings_item_language_es_item_text)
                                    Intl.ENGLISH -> stringResource(Res.string.dialog_settings_item_language_en_item_text)
                                    else -> Locale(language).displayLanguage.replaceFirstChar { it.uppercase() }
                                })
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