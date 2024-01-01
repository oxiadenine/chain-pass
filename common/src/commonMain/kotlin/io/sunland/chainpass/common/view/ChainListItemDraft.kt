package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.component.ValidationTextField

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainListItemDraft(chain: Chain, onNew: () -> Unit, onCancel: () -> Unit) {
    val focusRequester = remember { FocusRequester() }

    val nameState = remember { mutableStateOf(chain.name.value) }
    val nameValidationState = remember { mutableStateOf(chain.name.validation) }

    val keyState = remember { mutableStateOf(chain.key.value) }
    val keyValidationState = remember { mutableStateOf(chain.key.validation) }

    val onNameChange = { value: String ->
        chain.name = Chain.Name(value)

        nameState.value = chain.name.value
        nameValidationState.value = chain.name.validation
    }

    val onKeyChange = { value: String ->
        chain.key = Chain.Key(value)

        keyState.value = chain.key.value
        keyValidationState.value = chain.key.validation
    }

    val onDone = {
        chain.name = Chain.Name(nameState.value)
        chain.key = Chain.Key(keyState.value)

        nameValidationState.value = chain.name.validation
        keyValidationState.value = chain.key.validation

        if (nameValidationState.value.isSuccess && keyValidationState.value.isSuccess) {
            onNew()
        }
    }

    val onClear = { onCancel() }

    val onKeyEvent = { keyEvent: KeyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            false
        } else when (keyEvent.key) {
            Key.Escape -> {
                onClear()

                true
            }
            Key.Enter -> {
                onDone()

                true
            }
            else -> false
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(all = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onDone
            ) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
            IconButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onClear
            ) { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
        }
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            ValidationTextField(
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onKeyEvent(onKeyEvent),
                placeholder = { Text(text = "Name") },
                value = nameState.value,
                onValueChange = onNameChange,
                trailingIcon = if (nameValidationState.value.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = nameValidationState.value.isFailure,
                errorMessage = nameValidationState.value.exceptionOrNull()?.message,
                singleLine = true,
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
                placeholder = { Text(text = "Key") },
                value = keyState.value,
                onValueChange = onKeyChange,
                leadingIcon = {
                    IconButton(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .pointerHoverIcon(icon = PointerIconDefaults.Hand)
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                                    onKeyChange(chain.generateKey())

                                    true
                                } else false
                            },
                        onClick = { onKeyChange(chain.generateKey()) }
                    ) { Icon(imageVector = Icons.Default.VpnKey, contentDescription = null) }
                },
                trailingIcon = if (keyValidationState.value.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = keyValidationState.value.isFailure,
                errorMessage = keyValidationState.value.exceptionOrNull()?.message,
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(onDone = { onDone() })
            )
        }
    }

    if (chain.isLatest) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}