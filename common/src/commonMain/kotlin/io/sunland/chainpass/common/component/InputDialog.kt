package io.sunland.chainpass.common.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.VisualTransformation

@Composable
expect fun InputDialog(
    title: @Composable (() -> Unit)?,
    placeholder: String,
    value: String,
    ontValueChange: (String) -> Unit,
    visualTransformation: VisualTransformation,
    isError: Boolean,
    keyboardOptions: KeyboardOptions,
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
)
