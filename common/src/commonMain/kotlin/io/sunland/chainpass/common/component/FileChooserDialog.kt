package io.sunland.chainpass.common.component

import androidx.compose.runtime.Composable

sealed class FileChooserResult {
    object None : FileChooserResult()
    data class File(val path: String, val bytes: ByteArray) : FileChooserResult()
}

@Composable
expect fun FileChooserDialog(
    isOpened: Boolean = false,
    fileExtensions: List<String> = emptyList(),
    onClose: (FileChooserResult) -> Unit
)