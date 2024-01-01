package io.sunland.chainpass.common.component

import androidx.compose.runtime.Composable

@Composable
expect fun Dialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
    content: @Composable (() -> Unit)? = null
)