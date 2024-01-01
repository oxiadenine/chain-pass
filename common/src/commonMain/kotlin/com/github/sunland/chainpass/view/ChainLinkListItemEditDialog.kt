package com.github.sunland.chainpass.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.sunland.chainpass.ChainLink
import com.github.sunland.chainpass.LocalIntl
import com.github.sunland.chainpass.component.InputDialog
import com.github.sunland.chainpass.component.ValidationTextField
import com.github.sunland.chainpass.security.PasswordGenerator

@Composable
fun ChainLinkListItemEditDialog(
    onConfirm: (ChainLink.Description, ChainLink.Password) -> Unit,
    onCancel: () -> Unit,
    chainLink: ChainLink,
    passwordGenerator: PasswordGenerator
) {
    val intl = LocalIntl.current

    var chainLinkDescription by remember { mutableStateOf(chainLink.description) }
    var chainLinkPassword by remember { mutableStateOf(chainLink.password) }

    val onDescriptionTextFieldValueChange = { description: String ->
        chainLinkDescription = ChainLink.Description(description)
    }

    val onPasswordTextFieldValueChange = { password: String ->
        chainLinkPassword = ChainLink.Password(password)
    }

    val onInputDialogConfirmRequest = {
        chainLinkDescription = ChainLink.Description(chainLinkDescription.value)
        chainLinkPassword = ChainLink.Password(chainLinkPassword.value)

        if (chainLinkDescription.validation.isSuccess && chainLinkPassword.validation.isSuccess) {
            onConfirm(chainLinkDescription, chainLinkPassword)
        }
    }

    InputDialog(onDismissRequest = onCancel, onConfirmRequest = onInputDialogConfirmRequest) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp).onKeyEvent { keyEvent: KeyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                    onInputDialogConfirmRequest()

                    true
                } else false
            },
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val focusRequester = remember { FocusRequester() }

            ValidationTextField(
                value = chainLinkDescription.value,
                onValueChange = onDescriptionTextFieldValueChange,
                placeholder = { Text(text = intl.translate("dialog.chainLink.textField.description.placeholder")) },
                trailingIcon = if (chainLinkDescription.validation.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = chainLinkDescription.validation.isFailure,
                errorMessage = chainLinkDescription.validation.exceptionOrNull()?.let { error ->
                    if (error is ChainLink.Description.LengthError) {
                       intl.translate("dialog.chainLink.textField.description.length.error")
                    } else null
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            ValidationTextField(
                value = chainLinkPassword.value,
                onValueChange = onPasswordTextFieldValueChange,
                modifier = Modifier.focusRequester(focusRequester = focusRequester),
                placeholder = { Text(text = intl.translate("dialog.chainLink.textField.password.placeholder")) },
                leadingIcon = {
                    IconButton(
                        onClick = { onPasswordTextFieldValueChange(passwordGenerator.generate()) },
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .pointerHoverIcon(icon = PointerIcon.Hand)
                            .onKeyEvent { keyEvent: KeyEvent ->
                                if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                                    onPasswordTextFieldValueChange(passwordGenerator.generate())

                                    true
                                } else false
                            }
                    ) { Icon(imageVector = Icons.Default.VpnKey, contentDescription = null) }
                },
                trailingIcon = if (chainLinkPassword.validation.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = chainLinkPassword.validation.isFailure,
                errorMessage = chainLinkPassword.validation.exceptionOrNull()?.let { error ->
                    when (error) {
                        is ChainLink.Password.EmptyError -> {
                            intl.translate("dialog.chainLink.textField.password.empty.error")
                        }
                        is ChainLink.Password.LengthError -> {
                            intl.translate("dialog.chainLink.textField.password.length.error")
                        }
                        else -> null
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(onDone = { onInputDialogConfirmRequest() })
            )

            LaunchedEffect(focusRequester) {
                focusRequester.requestFocus()
            }
        }
    }
}