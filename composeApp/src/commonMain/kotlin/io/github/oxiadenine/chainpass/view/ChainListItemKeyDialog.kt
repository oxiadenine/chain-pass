package io.github.oxiadenine.chainpass.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.oxiadenine.chainpass.Chain
import io.github.oxiadenine.chainpass.component.InputDialog
import io.github.oxiadenine.chainpass.component.ValidationTextField
import io.github.oxiadenine.composeapp.generated.resources.Res
import io.github.oxiadenine.composeapp.generated.resources.dialog_chain_textField_key_empty_error
import io.github.oxiadenine.composeapp.generated.resources.dialog_chain_textField_key_length_error
import io.github.oxiadenine.composeapp.generated.resources.dialog_chain_textField_key_placeholder
import org.jetbrains.compose.resources.stringResource

data class ChainListItemKeyDialogState(
    val key: Chain.Key = Chain.Key(),
    val keyVisible: Boolean = false
) {
    companion object {
        val Saver = listSaver(
            save = { state ->
                listOf(state.value.key.value, state.value.key.validation.isFailure, state.value.keyVisible)
            },
            restore = {
                val key = it[0] as String
                val keyError = it[1] as Boolean

                mutableStateOf(ChainListItemKeyDialogState(
                    key = if (key.isNotEmpty() || keyError) {
                        Chain.Key(key)
                    } else Chain.Key(),
                    keyVisible = it[2] as Boolean
                ))
            }
        )
    }
}

@Composable
fun rememberChainListItemKeyDialogState() = rememberSaveable(saver = ChainListItemKeyDialogState.Saver) {
    mutableStateOf(ChainListItemKeyDialogState())
}

@Composable
fun ChainListItemKeyDialog(onConfirm: (Chain.Key) -> Unit, onCancel: () -> Unit) {
    var state by rememberChainListItemKeyDialogState()

    val onKeyTextFieldValueChange = { key: String ->
        state = state.copy(key = Chain.Key(key))
    }

    val onKeyVisibilityIconClick = {
        state = state.copy(keyVisible = !state.keyVisible)
    }

    val onInputDialogConfirmRequest = {
        state = state.copy(key = Chain.Key(state.key.value))

        if (state.key.validation.isSuccess) {
            onConfirm(state.key)
        }
    }

    InputDialog(onDismissRequest = onCancel, onConfirmRequest = onInputDialogConfirmRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp)
                .onKeyEvent { keyEvent: KeyEvent ->
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
                value = state.key.value,
                onValueChange = onKeyTextFieldValueChange,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester = focusRequester),
                placeholder = {
                    Text(text = stringResource(Res.string.dialog_chain_textField_key_placeholder))
                },
                trailingIcon = {
                    if (state.key.validation.isFailure) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null)
                    } else {
                        IconButton(
                            onClick = onKeyVisibilityIconClick,
                            modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                        ) {
                            AnimatedVisibility(
                                visible = !state.keyVisible,
                                enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 500))
                            ) { Icon(imageVector = Icons.Default.Visibility, contentDescription = null) }
                            AnimatedVisibility(
                                visible = state.keyVisible,
                                enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 500))
                            ) { Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = null) }
                        }
                    }
                },
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
                visualTransformation = if (!state.keyVisible) {
                    PasswordVisualTransformation()
                } else VisualTransformation.None,
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