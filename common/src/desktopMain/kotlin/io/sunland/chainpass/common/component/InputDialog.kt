package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun InputDialog(
    title: @Composable (() -> Unit)?,
    placeholder: String,
    value: String,
    ontValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation,
    isError: Boolean,
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) = PopupAlertDialogProvider.AlertDialog(onDismissRequest) {
    title ?: Text(text = "")
    Column(
        modifier = Modifier.size(width = 300.dp, height = 170.dp).padding(horizontal = 16.dp).padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        TextField(
            modifier = Modifier.padding(horizontal = 16.dp),
            placeholder = { Text(text = placeholder) },
            value = value,
            onValueChange = ontValueChange,
            trailingIcon = if (isError) {
                { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
            } else null,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            isError = isError
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
    }
}
