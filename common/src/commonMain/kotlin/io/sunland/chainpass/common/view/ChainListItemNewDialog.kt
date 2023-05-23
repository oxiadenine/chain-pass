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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.LocalIntl
import io.sunland.chainpass.common.component.InputDialog
import io.sunland.chainpass.common.component.ValidationTextField
import io.sunland.chainpass.common.security.PasswordGenerator

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChainListItemNewDialog(
    onConfirm: (Chain.Name, Chain.Key) -> Unit,
    onCancel: () -> Unit,
    passwordGenerator: PasswordGenerator
) {
    val intl = LocalIntl.current

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
                placeholder = { Text(text = intl.translate("dialog.chain.textField.name.placeholder")) },
                trailingIcon = if (chainName.validation.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = chainName.validation.isFailure,
                errorMessage = chainName.validation.exceptionOrNull()?.let { error ->
                    when (error) {
                        is Chain.Name.EmptyError -> {
                            intl.translate("dialog.chain.textField.name.empty.error")
                        }
                        is Chain.Name.LengthError -> {
                            intl.translate("dialog.chain.textField.name.length.error")
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
                value = chainKey.value,
                onValueChange = onKeyTextFieldValueChange,
                placeholder = { Text(text = intl.translate("dialog.chain.textField.key.placeholder")) },
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
                            intl.translate("dialog.chain.textField.key.empty.error")
                        }
                        is Chain.Key.LengthError -> {
                            intl.translate("dialog.chain.textField.key.length.error")
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