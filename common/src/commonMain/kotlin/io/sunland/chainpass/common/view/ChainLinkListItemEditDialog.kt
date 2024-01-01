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
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.component.InputDialog
import io.sunland.chainpass.common.component.ValidationTextField

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkListItemEditDialog(chainLink: ChainLink, onEdit: () -> Unit, onCancel: () -> Unit) {
    val descriptionState = remember { mutableStateOf(chainLink.description.value) }
    val descriptionValidationState = remember { mutableStateOf(chainLink.description.validation) }

    val passwordState = remember { mutableStateOf(chainLink.password.value) }
    val passwordValidationState = remember { mutableStateOf(chainLink.password.validation) }

    val onDescriptionChange = { description: String ->
        chainLink.description = ChainLink.Description(description)

        descriptionState.value = chainLink.description.value
        descriptionValidationState.value = chainLink.description.validation
    }

    val onPasswordChange = { password: String ->
        chainLink.password = ChainLink.Password(password)

        passwordState.value = chainLink.password.value
        passwordValidationState.value = chainLink.password.validation
    }

    val onDone = {
        chainLink.description = ChainLink.Description(descriptionState.value)
        chainLink.password = ChainLink.Password(passwordState.value)

        descriptionValidationState.value = chainLink.description.validation
        passwordValidationState.value = chainLink.password.validation

        if (descriptionValidationState.value.isSuccess && passwordValidationState.value.isSuccess) {
            onEdit()
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
            onPasswordChange(chainLink.generatePassword())

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
                value = descriptionState.value,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth().onKeyEvent(onKeyEvent = onKeyEvent),
                placeholder = { Text(text = "Description") },
                trailingIcon = if (descriptionValidationState.value.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = descriptionValidationState.value.isFailure,
                errorMessage = descriptionValidationState.value.exceptionOrNull()?.message,
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
                value = passwordState.value,
                onValueChange = onPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester = focusRequester)
                    .onKeyEvent(onKeyEvent = onKeyEvent),
                placeholder = { Text(text = "Password") },
                leadingIcon = {
                    IconButton(
                        onClick = { onPasswordChange(chainLink.generatePassword()) },
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .pointerHoverIcon(icon = PointerIconDefaults.Hand)
                            .onPreviewKeyEvent(onPreviewKeyEvent = onPreviewKeyEvent)
                    ) { Icon(imageVector = Icons.Default.VpnKey, contentDescription = null) }
                },
                trailingIcon = if (passwordValidationState.value.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = passwordValidationState.value.isFailure,
                errorMessage = passwordValidationState.value.exceptionOrNull()?.message,
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