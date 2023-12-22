package com.github.sunland.chainpass.view

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
import com.github.sunland.chainpass.Chain
import com.github.sunland.chainpass.LocalIntl
import com.github.sunland.chainpass.component.InputDialog
import com.github.sunland.chainpass.component.ValidationTextField

@Composable
fun ChainListItemKeyDialog(onConfirm: (Chain.Key) -> Unit, onCancel: () -> Unit) {
    val intl = LocalIntl.current

    var chainKey by remember { mutableStateOf(Chain.Key()) }

    var keyVisible by remember { mutableStateOf(false) }

    val onKeyTextFieldValueChange = { key: String ->
        chainKey = Chain.Key(key)
    }

    val onKeyVisibilityIconClick = {
        keyVisible = !keyVisible
    }

    val onInputDialogConfirmRequest = {
        chainKey = Chain.Key(chainKey.value)

        if (chainKey.validation.isSuccess) {
            onConfirm(chainKey)
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
                value = chainKey.value,
                onValueChange = onKeyTextFieldValueChange,
                modifier = Modifier.focusRequester(focusRequester = focusRequester),
                placeholder = { Text(text = intl.translate("dialog.chain.textField.key.placeholder")) },
                trailingIcon = {
                    if (chainKey.validation.isFailure) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null)
                    } else {
                        IconButton(
                            onClick = onKeyVisibilityIconClick,
                            modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                        ) {
                            AnimatedVisibility(
                                visible = !keyVisible,
                                enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 500))
                            ) { Icon(imageVector = Icons.Default.Visibility, contentDescription = null) }
                            AnimatedVisibility(
                                visible = keyVisible,
                                enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 500))
                            ) { Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = null) }
                        }
                    }
                },
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
                visualTransformation = if (!keyVisible) {
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