package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.component.InputDialog
import io.sunland.chainpass.common.component.ValidationTextField

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChainListItemNewDialog(chain: Chain, onNew: (Chain) -> Unit, onCancel: () -> Unit) {
    val nameState = remember { mutableStateOf(chain.name.value) }
    val nameValidationState = remember { mutableStateOf(chain.name.validation) }

    val keyState = remember { mutableStateOf(chain.key.value) }
    val keyValidationState = remember { mutableStateOf(chain.key.validation) }

    val onNameChange = { name: String ->
        chain.name = Chain.Name(name)

        nameState.value = chain.name.value
        nameValidationState.value = chain.name.validation
    }

    val onKeyChange = { key: String ->
        chain.key = Chain.Key(key)

        keyState.value = chain.key.value
        keyValidationState.value = chain.key.validation
    }

    val onDone = {
        chain.name = Chain.Name(nameState.value)
        chain.key = Chain.Key(keyState.value)

        nameValidationState.value = chain.name.validation
        keyValidationState.value = chain.key.validation

        if (nameValidationState.value.isSuccess && keyValidationState.value.isSuccess) {
            onNew(chain)
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

    val onPreviewKeyEvent = { keyEvent: KeyEvent ->
        if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
            onKeyChange(chain.generateKey())

            true
        } else false
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
                value = nameState.value,
                onValueChange = onNameChange,
                modifier = Modifier
                    .focusRequester(focusRequester = focusRequester)
                    .onKeyEvent(onKeyEvent = onKeyEvent),
                placeholder = { Text(text = "Name") },
                trailingIcon = if (nameValidationState.value.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = nameValidationState.value.isFailure,
                errorMessage = nameValidationState.value.exceptionOrNull()?.message,
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            ValidationTextField(
                value = keyState.value,
                onValueChange = onKeyChange,
                modifier = Modifier.onKeyEvent(onKeyEvent = onKeyEvent),
                placeholder = { Text(text = "Key") },
                leadingIcon = {
                    IconButton(
                        onClick = { onKeyChange(chain.generateKey()) },
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .pointerHoverIcon(icon = PointerIconDefaults.Hand)
                            .onPreviewKeyEvent(onPreviewKeyEvent = onPreviewKeyEvent)
                    ) { Icon(imageVector = Icons.Default.VpnKey, contentDescription = null) }
                },
                trailingIcon = if (keyValidationState.value.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = keyValidationState.value.isFailure,
                errorMessage = keyValidationState.value.exceptionOrNull()?.message,
                singleLine = true,
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