package io.github.oxiadenine.chainpass.view

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
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

data class ChainListItemNewDialogState(
    val name: Chain.Name = Chain.Name(),
    val key: Chain.Key = Chain.Key()
) {
    companion object {
        val Saver = listSaver(
            save = { state ->
                listOf(
                    state.value.name.value,
                    state.value.name.validation.isFailure,
                    state.value.key.value,
                    state.value.key.validation.isFailure
                )
            },
            restore = {
                val name = it[0] as String
                val nameError = it[1] as Boolean
                val key = it[2] as String
                val keyError = it[3] as Boolean

                mutableStateOf(ChainListItemNewDialogState(
                    name = if (name.isNotEmpty() || nameError) {
                        Chain.Name(name)
                    } else Chain.Name(),
                    key = if (key.isNotEmpty() || keyError) {
                        Chain.Key(key)
                    } else Chain.Key()
                ))
            }
        )
    }
}

@Composable
fun rememberChainListItemNewDialogState() = rememberSaveable(saver = ChainListItemNewDialogState.Saver) {
    mutableStateOf(ChainListItemNewDialogState())
}

@Composable
fun ChainListItemNewDialog(
    onConfirm: (Chain.Name, Chain.Key) -> Unit,
    onCancel: () -> Unit,
    passwordGenerator: PasswordGenerator
) {
    var state by rememberChainListItemNewDialogState()

    val onNameTextFieldValueChange = { name: String ->
        state = state.copy(name = Chain.Name(name))
    }

    val onKeyTextFieldValueChange = { key: String ->
        state = state.copy(key = Chain.Key(key))
    }

    val onInputDialogConfirmRequest = {
        state = state.copy(name = Chain.Name(state.name.value), key = Chain.Key(state.key.value))

        if (state.name.validation.isSuccess && state.key.validation.isSuccess) {
            onConfirm(state.name, state.key)
        }
    }

    InputDialog(onDismissRequest = onCancel, onConfirmRequest = onInputDialogConfirmRequest) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp).onKeyEvent { keyEvent: KeyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                    onInputDialogConfirmRequest()

                    true
                } else false
            }.focusGroup(),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val focusRequester = remember { FocusRequester() }

            ValidationTextField(
                value = state.name.value,
                onValueChange = onNameTextFieldValueChange,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester = focusRequester),
                placeholder = {
                    Text(text = stringResource(Res.string.dialog_chain_textField_name_placeholder))
                },
                trailingIcon = if (state.name.validation.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = state.name.validation.isFailure,
                errorMessage = state.name.validation.exceptionOrNull()?.let { error ->
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
                value = state.key.value,
                onValueChange = onKeyTextFieldValueChange,
                modifier = Modifier.fillMaxWidth(),
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
                trailingIcon = if (state.key.validation.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = state.key.validation.isFailure,
                errorMessage = state.key.validation.exceptionOrNull()?.let { error ->
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