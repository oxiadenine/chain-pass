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
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.component.ValidationTextField
import io.sunland.chainpass.common.security.GeneratorSpec
import io.sunland.chainpass.common.security.PasswordGenerator

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkListItemEdit(chainLink: ChainLink, onEdit: () -> Unit, onCancel: () -> Unit) {
    val focusRequester = remember { FocusRequester() }

    val isEdited = chainLink.description.isEdited || chainLink.password.isEdited

    val isEditedState = remember { mutableStateOf(isEdited) }

    val descriptionState = remember { mutableStateOf(chainLink.description.value) }
    val descriptionValidationState = remember { mutableStateOf(chainLink.description.validation) }

    val passwordState = remember { mutableStateOf(chainLink.password.value) }
    val passwordValidationState = remember { mutableStateOf(chainLink.password.validation) }

    val onDescriptionChange = { value: String ->
        chainLink.description = ChainLink.Description(value, isEdited = true)

        descriptionState.value = chainLink.description.value
        descriptionValidationState.value = chainLink.description.validation

        isEditedState.value = chainLink.password.isEdited || chainLink.description.isEdited
    }

    val onPasswordChange = { value: String ->
        chainLink.password = ChainLink.Password(value, isEdited = true)

        passwordState.value = chainLink.password.value
        passwordValidationState.value = chainLink.password.validation

        isEditedState.value = chainLink.password.isEdited || chainLink.description.isEdited
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

    val onClear = {
        descriptionState.value = chainLink.description.value
        descriptionValidationState.value = chainLink.description.validation

        passwordState.value = chainLink.password.value
        passwordValidationState.value = chainLink.password.validation

        onCancel()
    }

    val onKeyEvent = { keyEvent: KeyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            false
        } else when (keyEvent.key) {
            Key.Escape -> {
                onClear()

                true
            }
            Key.Enter -> {
                if (isEditedState.value) {
                    onDone()
                }

                true
            }
            else -> false
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(modifier = Modifier.padding(horizontal = 16.dp), text = chainLink.name.value)
            Row(
                modifier = Modifier.padding(all = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditedState.value) {
                    IconButton(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = onDone
                    ) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
                }
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = onClear
                ) { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
            }
        }
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            val passwordGenerator = PasswordGenerator(GeneratorSpec.Strength(16))

            ValidationTextField(
                modifier = Modifier.fillMaxWidth().onKeyEvent(onKeyEvent),
                placeholder = { Text(text = "Description", fontSize = 14.sp) },
                value = descriptionState.value,
                onValueChange = onDescriptionChange,
                trailingIcon = if (descriptionValidationState.value.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = descriptionValidationState.value.isFailure,
                errorMessage = descriptionValidationState.value.exceptionOrNull()?.message,
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
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onKeyEvent(onKeyEvent),
                placeholder = { Text(text = "Password") },
                value = passwordState.value,
                onValueChange = onPasswordChange,
                leadingIcon = {
                    IconButton(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .pointerHoverIcon(icon = PointerIconDefaults.Hand)
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                                    onPasswordChange(passwordGenerator.generate())

                                    true
                                } else false
                            },
                        onClick = { onPasswordChange(passwordGenerator.generate()) }
                    ) { Icon(imageVector = Icons.Default.VpnKey, contentDescription = null) }
                },
                trailingIcon = if (passwordValidationState.value.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = passwordValidationState.value.isFailure,
                errorMessage = passwordValidationState.value.exceptionOrNull()?.message,
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(onDone = {
                    if (isEditedState.value) {
                        onDone()
                    }
                })
            )
        }
    }

    if (chainLink.isLatest) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}