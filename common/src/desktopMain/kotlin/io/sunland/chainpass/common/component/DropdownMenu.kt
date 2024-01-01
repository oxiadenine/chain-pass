package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset

@Composable
actual fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    offset: DpOffset,
    content: @Composable (ColumnScope.() -> Unit)
) = androidx.compose.material.DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    offset = offset,
    content = content
)

@Composable
actual fun DropdownMenuItem(
    onClick: () -> Unit,
    modifier: Modifier,
    contentPadding: PaddingValues,
    content: @Composable (RowScope.() -> Unit)
) = androidx.compose.material.DropdownMenuItem(
    onClick = onClick,
    modifier = modifier,
    contentPadding = contentPadding,
    content = content
)