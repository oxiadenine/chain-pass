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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
actual fun InputDialog(
    title: @Composable (() -> Unit)?,
    placeholder: String,
    value: String,
    ontValueChange: (String) -> Unit,
    visualTransformation: VisualTransformation,
    isError: Boolean,
    keyboardOptions: KeyboardOptions,
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = title ?: { Text(text = "") },
        text = {
            Column {
                TextField(
                    modifier = Modifier.padding(horizontal = 8.dp).focusRequester(focusRequester),
                    placeholder = { Text(text = placeholder) },
                    value = value,
                    onValueChange = ontValueChange,
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
            }
        },
        buttons = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                Row(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                    TextButton(onClick = onDismissRequest) { Text(text = "Cancel") }
                    TextButton(onClick = onConfirmRequest) { Text(text = "Ok") }
                }
            }
        }
    )

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
