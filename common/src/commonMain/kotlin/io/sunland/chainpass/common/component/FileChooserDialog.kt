package io.sunland.chainpass.common.component

import androidx.compose.runtime.Composable

@Composable
expect fun FileChooserDialog(
    isOpened: Boolean = false,
    fileExtensions: List<String> = emptyList(),
    onClose: (String) -> Unit
)