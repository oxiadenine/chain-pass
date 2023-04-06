package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import io.sunland.chainpass.common.LocalIntl
import io.sunland.chainpass.common.component.InputDialog
import io.sunland.chainpass.common.component.ValidationTextField
import io.sunland.chainpass.common.security.PasswordGenerator

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChainLinkListItemNewDialog(
    onConfirm: (ChainLink.Name, ChainLink.Description, ChainLink.Password) -> Unit,
    onCancel: () -> Unit,
    passwordGenerator: PasswordGenerator
) {
    val intl = LocalIntl.current

    var chainLinkName by remember { mutableStateOf(ChainLink.Name()) }
    var chainLinkDescription by remember { mutableStateOf(ChainLink.Description()) }
    var chainLinkPassword by remember { mutableStateOf(ChainLink.Password()) }

    val onNameTextFieldValueChange = { name: String ->
        chainLinkName = ChainLink.Name(name)
    }

    val onDescriptionTextFieldValueChange = { description: String ->
        chainLinkDescription = ChainLink.Description(description)
    }

    val onPasswordTextFieldValueChange = { password: String ->
        chainLinkPassword = ChainLink.Password(password)
    }

    val onInputDialogConfirmRequest = {
        chainLinkName = ChainLink.Name(chainLinkName.value)
        chainLinkDescription = ChainLink.Description(chainLinkDescription.value)
        chainLinkPassword = ChainLink.Password(chainLinkPassword.value)

        if (chainLinkName.validation.isSuccess &&
            chainLinkDescription.validation.isSuccess && chainLinkPassword.validation.isSuccess
            ) { onConfirm(chainLinkName, chainLinkDescription, chainLinkPassword) }
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
                value = chainLinkName.value,
                onValueChange = onNameTextFieldValueChange,
                modifier = Modifier.focusRequester(focusRequester = focusRequester),
                placeholder = { Text(text = intl.translate("dialog.chainLink.textField.name.placeholder")) },
                trailingIcon = if (chainLinkName.validation.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = chainLinkName.validation.isFailure,
                errorMessage = chainLinkName.validation.exceptionOrNull()?.let { error ->
                    when (error) {
                        is ChainLink.Name.EmptyError -> {
                            intl.translate("dialog.chainLink.textField.name.empty.error")
                        }
                        is ChainLink.Name.LengthError -> {
                            intl.translate("dialog.chainLink.textField.name.length.error")
                        }
                        else -> null
                    }
                },
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
                colors = TextFieldDefaults.textFieldColors(
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
                placeholder = { Text(text = intl.translate("dialog.chainLink.textField.password.placeholder")) },
                leadingIcon = {
                    IconButton(
                        onClick = { onPasswordTextFieldValueChange(passwordGenerator.generate()) },
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .pointerHoverIcon(icon = PointerIconDefaults.Hand)
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
                colors = TextFieldDefaults.textFieldColors(
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