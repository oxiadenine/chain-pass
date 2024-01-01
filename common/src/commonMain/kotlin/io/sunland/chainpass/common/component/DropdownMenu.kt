package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset

@Composable
expect fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    offset: DpOffset,
    content: @Composable (ColumnScope.() -> Unit)
)

@Composable
expect fun DropdownMenuItem(
    onClick: () -> Unit,
    modifier: Modifier,
    contentPadding: PaddingValues,
    content: @Composable (RowScope.() -> Unit)
)
