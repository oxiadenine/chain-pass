package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
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
fun ChainLinkListItemEdit(chainLink: ChainLink, onIconDoneClick: () -> Unit, onIconClearClick: () -> Unit) {
    val isEdited = remember { mutableStateOf(false) }

    val descriptionState = remember { mutableStateOf(chainLink.description.value) }
    val descriptionValidationState = remember { mutableStateOf(chainLink.description.validation) }

    val passwordState = remember { mutableStateOf(chainLink.password.value) }
    val passwordValidationState = remember { mutableStateOf(chainLink.password.validation) }

    val onDescriptionChange = { value: String ->
        isEdited.value = value != chainLink.description.value || passwordState.value != chainLink.password.value

        val chainLinkDescription = ChainLink.Description(value)

        descriptionState.value = chainLinkDescription.value
        descriptionValidationState.value = chainLinkDescription.validation
    }

    val onPasswordChange = { value: String ->
        isEdited.value = value != chainLink.password.value || descriptionState.value != chainLink.description.value

        val chainLinkPassword = ChainLink.Password(value)

        passwordState.value = chainLinkPassword.value
        passwordValidationState.value = chainLinkPassword.validation
    }

    val onDone = {
        val chainLinkDescription = ChainLink.Description(descriptionState.value)
        val chainLinkPassword = ChainLink.Password(passwordState.value)

        descriptionValidationState.value = chainLinkDescription.validation
        passwordValidationState.value = chainLinkPassword.validation

        if (descriptionValidationState.value.isSuccess && passwordValidationState.value.isSuccess) {
            chainLink.description = chainLinkDescription
            chainLink.password = chainLinkPassword

            onIconDoneClick()
        }
    }

    val onClear = {
        descriptionState.value = chainLink.description.value
        descriptionValidationState.value = chainLink.description.validation

        passwordState.value = chainLink.password.value
        passwordValidationState.value = chainLink.password.validation

        onIconClearClick()
    }

    val onKeyEvent = { keyEvent: KeyEvent ->
        when (keyEvent.key) {
            Key.Escape -> {
                onClear()

                true
            }
            Key.Enter -> {
                if (isEdited.value) {
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
                if (isEdited.value) {
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
            val focusManager = LocalFocusManager.current
            val focusRequester = remember { FocusRequester() }

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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
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
                                if (keyEvent.key == Key.Enter) {
                                    onPasswordChange(passwordGenerator.generate())

                                    true
                                } else false
                            },
                        onClick = { onPasswordChange(passwordGenerator.generate()) }
                    ) { Icon(imageVector = Icons.Default.Build, contentDescription = null) }
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
                keyboardActions = KeyboardActions(onDone = { if (isEdited.value) onDone() })
            )

            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        }
    }
}