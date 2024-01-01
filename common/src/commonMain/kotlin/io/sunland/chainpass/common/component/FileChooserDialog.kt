package io.sunland.chainpass.common.component

import androidx.compose.runtime.Composable

sealed class FileChooserResult {
    data object None : FileChooserResult()
    data class File(val path: String, val bytes: ByteArray) : FileChooserResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as File

            if (path != other.path) return false
            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = path.hashCode()
            result = 31 * result + bytes.contentHashCode()
            return result
        }
    }
}

@Composable
expect fun FileChooserDialog(
    isOpened: Boolean = false,
    fileExtensions: List<String> = emptyList(),
    onClose: (FileChooserResult) -> Unit
)