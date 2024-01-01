package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun InputDialog(
    title: @Composable (() -> Unit)?,
    placeholder: @Composable (() -> Unit)?,
    value: String,
    onValueChange: (String) -> Unit,
    visualTransformation: VisualTransformation,
    isError: Boolean,
    keyboardOptions: KeyboardOptions,
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) = PopupAlertDialogProvider.AlertDialog(onDismissRequest) {
    title ?: Text(text = "")

    Column(
        modifier = Modifier
            .size(width = 300.dp, height = 170.dp)
            .padding(horizontal = 16.dp).padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        val focusRequester = remember { FocusRequester() }

        TextField(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester)
                .onKeyEvent { keyEvent ->
                    when (keyEvent.key) {
                        Key.Escape -> {
                            onDismissRequest()

                            true
                        }
                        Key.Enter -> {
                            onConfirmRequest()

                            true
                        }
                        else -> false
                    }
                },
            placeholder = placeholder,
            value = value,
            onValueChange = onValueChange,
            trailingIcon = if (isError) {
                { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
            } else null,
            visualTransformation = visualTransformation,
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = KeyboardActions(onDone = { onConfirmRequest() })
        )
        Row(
            modifier = Modifier.fillMaxWidth().align(alignment = Alignment.End).padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onDismissRequest
            ) { Text(text = "Cancel") }
            TextButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onConfirmRequest
            ) { Text(text = "Ok") }
        }

        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}