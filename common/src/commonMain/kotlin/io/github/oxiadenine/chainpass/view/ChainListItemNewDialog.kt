package io.github.oxiadenine.chainpass.view

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
import io.github.oxiadenine.chainpass.Chain
import io.github.oxiadenine.chainpass.component.InputDialog
import io.github.oxiadenine.chainpass.component.ValidationTextField
import io.github.oxiadenine.chainpass.security.PasswordGenerator
import io.github.oxiadenine.common.generated.resources.*
import io.github.oxiadenine.common.generated.resources.Res
import io.github.oxiadenine.common.generated.resources.dialog_chain_textField_name_empty_error
import io.github.oxiadenine.common.generated.resources.dialog_chain_textField_name_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChainListItemNewDialog(
    onConfirm: (Chain.Name, Chain.Key) -> Unit,
    onCancel: () -> Unit,
    passwordGenerator: PasswordGenerator
) {
    var chainName by remember { mutableStateOf(Chain.Name()) }
    var chainKey by remember { mutableStateOf(Chain.Key()) }

    val onNameTextFieldValueChange = { name: String ->
        chainName = Chain.Name(name)
    }

    val onKeyTextFieldValueChange = { key: String ->
        chainKey = Chain.Key(key)
    }

    val onInputDialogConfirmRequest = {
        chainName = Chain.Name(chainName.value)
        chainKey = Chain.Key(chainKey.value)

        if (chainName.validation.isSuccess && chainKey.validation.isSuccess) {
            onConfirm(chainName, chainKey)
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
                value = chainName.value,
                onValueChange = onNameTextFieldValueChange,
                modifier = Modifier.focusRequester(focusRequester = focusRequester),
                placeholder = {
                    Text(text = stringResource(Res.string.dialog_chain_textField_name_placeholder))
                },
                trailingIcon = if (chainName.validation.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = chainName.validation.isFailure,
                errorMessage = chainName.validation.exceptionOrNull()?.let { error ->
                    when (error) {
                        is Chain.Name.EmptyError -> {
                            stringResource(Res.string.dialog_chain_textField_name_empty_error)
                        }
                        is Chain.Name.LengthError -> {
                            stringResource(Res.string.dialog_chain_textField_name_length_error)
                        }
                        is Chain.Name.InvalidError -> {
                            stringResource(Res.string.dialog_chain_textField_name_invalid_error)
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            ValidationTextField(
                value = chainKey.value,
                onValueChange = onKeyTextFieldValueChange,
                placeholder = {
                    Text(text = stringResource(Res.string.dialog_chain_textField_key_placeholder))
                },
                leadingIcon = {
                    IconButton(
                        onClick = { onKeyTextFieldValueChange(passwordGenerator.generate()) },
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .pointerHoverIcon(icon = PointerIcon.Hand)
                            .onKeyEvent { keyEvent: KeyEvent ->
                                if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                                    onKeyTextFieldValueChange(passwordGenerator.generate())

                                    true
                                } else false
                            }
                    ) { Icon(imageVector = Icons.Default.VpnKey, contentDescription = null) }
                },
                trailingIcon = if (chainKey.validation.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = chainKey.validation.isFailure,
                errorMessage = chainKey.validation.exceptionOrNull()?.let { error ->
                    when (error) {
                        is Chain.Key.EmptyError -> {
                            stringResource(Res.string.dialog_chain_textField_key_empty_error)
                        }
                        is Chain.Key.LengthError -> {
                            stringResource(Res.string.dialog_chain_textField_key_length_error)
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