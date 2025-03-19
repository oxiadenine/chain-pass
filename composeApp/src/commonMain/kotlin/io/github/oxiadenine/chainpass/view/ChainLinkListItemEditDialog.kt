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
import io.github.oxiadenine.chainpass.ChainLink
import io.github.oxiadenine.chainpass.component.InputDialog
import io.github.oxiadenine.chainpass.component.ValidationTextField
import io.github.oxiadenine.chainpass.security.PasswordGenerator
import io.github.oxiadenine.composeapp.generated.resources.*
import io.github.oxiadenine.composeapp.generated.resources.Res
import io.github.oxiadenine.composeapp.generated.resources.dialog_chainLink_textField_description_length_error
import io.github.oxiadenine.composeapp.generated.resources.dialog_chainLink_textField_description_placeholder
import io.github.oxiadenine.composeapp.generated.resources.dialog_chainLink_textField_password_placeholder
import org.jetbrains.compose.resources.stringResource

data class ChainLinkListItemEditDialogState(
     val description: ChainLink.Description = ChainLink.Description(),
     val password: ChainLink.Password = ChainLink.Password()
) {
    companion object {
        val Saver = listSaver(
            save = { state ->
                listOf(
                    state.value.description.value,
                    state.value.description.validation.isFailure,
                    state.value.password.value,
                    state.value.password.validation.isFailure
                )
            },
            restore = {
                val description = it[0] as String
                val descriptionError = it[1] as Boolean
                val password = it[2] as String
                val passwordError = it[3] as Boolean

                mutableStateOf(ChainLinkListItemEditDialogState(
                    description = if (description.isNotEmpty() || descriptionError) {
                        ChainLink.Description(description)
                    } else ChainLink.Description(),
                    password = if (password.isNotEmpty() || passwordError) {
                        ChainLink.Password(password)
                    } else ChainLink.Password()
                ))
            }
        )
    }
}

@Composable
fun rememberChainLinkListItemEditDialogState(chainLink: ChainLink) = rememberSaveable(
    saver = ChainLinkListItemEditDialogState.Saver
) { mutableStateOf(ChainLinkListItemEditDialogState(chainLink.description, chainLink.password)) }

@Composable
fun ChainLinkListItemEditDialog(
    onConfirm: (ChainLink.Description, ChainLink.Password) -> Unit,
    onCancel: () -> Unit,
    chainLink: ChainLink,
    passwordGenerator: PasswordGenerator
) {
    var state by rememberChainLinkListItemEditDialogState(chainLink)

    val onDescriptionTextFieldValueChange = { description: String ->
        state = state.copy(description = ChainLink.Description(description))
    }

    val onPasswordTextFieldValueChange = { password: String ->
        state = state.copy(password = ChainLink.Password(password))
    }

    val onInputDialogConfirmRequest = {
        state = state.copy(
            description = ChainLink.Description(state.description.value),
            password = ChainLink.Password(state.password.value)
        )

        if (state.description.validation.isSuccess && state.password.validation.isSuccess) {
            onConfirm(state.description, state.password)
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
                value = state.description.value,
                onValueChange = onDescriptionTextFieldValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = stringResource(Res.string.dialog_chainLink_textField_description_placeholder)) },
                trailingIcon = if (state.description.validation.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = state.description.validation.isFailure,
                errorMessage = state.description.validation.exceptionOrNull()?.let { error ->
                    if (error is ChainLink.Description.LengthError) {
                        stringResource(Res.string.dialog_chainLink_textField_description_length_error)
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
                value = state.password.value,
                onValueChange = onPasswordTextFieldValueChange,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester = focusRequester),
                placeholder = {
                    Text(text = stringResource(Res.string.dialog_chainLink_textField_password_placeholder))
                },
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
                trailingIcon = if (state.password.validation.isFailure) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = state.password.validation.isFailure,
                errorMessage = state.password.validation.exceptionOrNull()?.let { error ->
                    when (error) {
                        is ChainLink.Password.EmptyError -> {
                            stringResource(Res.string.dialog_chainLink_textField_password_empty_error)
                        }
                        is ChainLink.Password.LengthError -> {
                            stringResource(Res.string.dialog_chainLink_textField_password_length_error)
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