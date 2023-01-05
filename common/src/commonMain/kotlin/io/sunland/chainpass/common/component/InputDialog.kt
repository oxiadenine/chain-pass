package io.sunland.chainpass.common.component

import androidx.compose.runtime.Composable

@Composable
expect fun InputDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
    content: @Composable (() -> Unit)? = null
)