package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.component.Dialog

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainListItemKeyInput(onDismiss: () -> Unit, onConfirm: (Chain.Key) -> Unit) {
    val focusRequester = remember { FocusRequester() }

    val keyState = remember { mutableStateOf("") }
    val keyErrorState = remember { mutableStateOf(false) }

    val onKeyChange = { value: String ->
        val chainKey = Chain.Key(value)

        keyState.value = chainKey.value
        keyErrorState.value = chainKey.validation.isFailure
    }

    val keyVisibleState = remember { mutableStateOf(false) }

    val onKeyVisibleChange = {
        keyVisibleState.value = !keyVisibleState.value
    }

    val onDone = {
        val chainKey = Chain.Key(keyState.value)

        keyErrorState.value = chainKey.validation.isFailure

        if (!keyErrorState.value) {
            onConfirm(chainKey)
        }
    }

    val onKeyEvent = { keyEvent: KeyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            false
        } else when (keyEvent.key) {
            Key.Escape -> {
                onDismiss()

                true
            }
            Key.Enter -> {
                onDone()

                true
            }
            else -> false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        onConfirmRequest = onDone
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onKeyEvent(onKeyEvent),
            placeholder = { Text(text = "Key") },
            value = keyState.value,
            onValueChange = onKeyChange,
            trailingIcon = {
                if (keyErrorState.value) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null)
                } else {
                    IconButton(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = onKeyVisibleChange
                    ) { Icon(imageVector = Icons.Default.Visibility, contentDescription = null) }
                }
            },
            isError = keyErrorState.value,
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

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}