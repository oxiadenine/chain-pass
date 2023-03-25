package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

@Composable
expect fun Dialog(
    onDismissRequest: () -> Unit,
    title: @Composable (() -> Unit)? = null,
    buttons: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
)