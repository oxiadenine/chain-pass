package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.LocalIntl
import io.sunland.chainpass.common.Platform
import io.sunland.chainpass.common.StorageType
import io.sunland.chainpass.common.component.FileChooserDialog
import io.sunland.chainpass.common.component.FileChooserResult
import io.sunland.chainpass.common.component.InputDialog
import io.sunland.chainpass.common.platform

class FileSelected(val path: String, val bytes: ByteArray) {
    val fileName = path.substringAfterLast("/").substringBeforeLast(".")
}

@Composable
fun UnstoreDialog(onConfirm: (FileSelected) -> Unit, onCancel: () -> Unit) {
    val intl = LocalIntl.current

    var fileChooserDialogOpened by remember { mutableStateOf(false) }
    var fileChooserDialogResult by remember { mutableStateOf<FileChooserResult?>(null) }

    val onSelectFileButtonClick = {
        fileChooserDialogOpened = true
    }

    val onCloseFileChooserDialog = { result: FileChooserResult ->
        fileChooserDialogOpened = false
        fileChooserDialogResult = result
    }

    val onInputDialogConfirmRequest = {
        if (fileChooserDialogResult == null) {
            fileChooserDialogResult = FileChooserResult.None
        }

        if (fileChooserDialogResult is FileChooserResult.File) {
            val file = fileChooserDialogResult as FileChooserResult.File

            onConfirm(FileSelected(file.path, file.bytes))
        }
    }

    InputDialog(
        onDismissRequest = onCancel,
        onConfirmRequest = onInputDialogConfirmRequest,
        title = { Text(text = intl.translate("dialog.unstore.title"), modifier = Modifier.padding(all = 16.dp)) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onSelectFileButtonClick,
                modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = intl.translate("dialog.unstore.item.button.file.text"))
                    Icon(imageVector = Icons.Default.FileOpen, contentDescription = null)
                }
            }

            if (fileChooserDialogResult != null) {
                if (fileChooserDialogResult is FileChooserResult.None) {
                    Text(
                        text = intl.translate("dialog.unstore.item.file.error"),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                } else {
                    val file = fileChooserDialogResult as FileChooserResult.File

                    Text(text = FileSelected(file.path, file.bytes).fileName, fontSize = 14.sp)
                }
            }
        }
    }

    val fileExtensions = if (platform == Platform.DESKTOP) {
        listOf(StorageType.JSON.name, StorageType.CSV.name)
    } else listOf("application/${StorageType.JSON.name.lowercase()}", "text/comma-separated-values")

    FileChooserDialog(
        isOpened = fileChooserDialogOpened,
        fileExtensions = fileExtensions.toList(),
        onClose = onCloseFileChooserDialog
    )
}