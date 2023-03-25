package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.component.InputDialog
import io.sunland.chainpass.common.component.ValidationTextField

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainListItemKeyDialog(onKey: (Chain.Key) -> Unit, onCancel: () -> Unit) {
    var chainKey = Chain.Key()

    val keyState = remember { mutableStateOf(chainKey.value) }
    val keyValidationState = remember { mutableStateOf(chainKey.validation) }

    val onKeyChange = { key: String ->
        chainKey = Chain.Key(key)

        keyState.value = chainKey.value
        keyValidationState.value = chainKey.validation
    }

    val keyVisibleState = remember { mutableStateOf(false) }

    val onKeyVisibleChange = {
        keyVisibleState.value = !keyVisibleState.value
    }

    val onDone = {
        chainKey = Chain.Key(keyState.value)

        keyValidationState.value = chainKey.validation

        if (keyValidationState.value.isSuccess) {
            onKey(chainKey)
        }
    }

    val onKeyEvent = { keyEvent: KeyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            false
        } else when (keyEvent.key) {
            Key.Escape -> {
                onCancel()

                true
            }
            Key.Enter -> {
                onDone()

                true
            }
            else -> false
        }
    }

    InputDialog(onDismissRequest = onCancel, onConfirmRequest = onDone) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(focusRequester) {
                focusRequester.requestFocus()
            }

            ValidationTextField(
                value = keyState.value,
                onValueChange = onKeyChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester = focusRequester)
                    .onKeyEvent(onKeyEvent = onKeyEvent),
                placeholder = { Text(text = "Key") },
                trailingIcon = {
                    if (keyValidationState.value.isFailure) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null)
                    } else {
                        IconButton(
                            onClick = onKeyVisibleChange,
                            modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
                        ) {
                            Icon(imageVector = if (keyVisibleState.value) {
                                Icons.Default.Visibility
                            } else Icons.Default.VisibilityOff, contentDescription = null)
                        }
                    }
                },
                isError = keyValidationState.value.isFailure,
                errorMessage = keyValidationState.value.exceptionOrNull()?.message,
                singleLine = true,
                visualTransformation = if (!keyVisibleState.value) {
                    PasswordVisualTransformation()
                } else VisualTransformation.None,
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(onDone = { onDone() })
            )
        }
    }
}