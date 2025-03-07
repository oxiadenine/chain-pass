package io.github.oxiadenine.chainpass.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.oxiadenine.chainpass.Platform
import io.github.oxiadenine.chainpass.StorageType
import io.github.oxiadenine.chainpass.component.FileChooserDialog
import io.github.oxiadenine.chainpass.component.FileChooserResult
import io.github.oxiadenine.chainpass.component.InputDialog
import io.github.oxiadenine.chainpass.platform
import io.github.oxiadenine.common.generated.resources.Res
import io.github.oxiadenine.common.generated.resources.dialog_unstore_item_button_file_text
import io.github.oxiadenine.common.generated.resources.dialog_unstore_item_file_error
import io.github.oxiadenine.common.generated.resources.dialog_unstore_title
import org.jetbrains.compose.resources.stringResource

class FileSelected(val path: String, val bytes: ByteArray) {
    val fileName = path.substringAfterLast("/").substringBeforeLast(".")
}

@Composable
fun UnstoreDialog(onConfirm: (FileSelected) -> Unit, onCancel: () -> Unit) {
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
        title = {
            Text(
                text = stringResource(Res.string.dialog_unstore_title),
                modifier = Modifier.padding(all = 16.dp)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val focusRequester = remember { FocusRequester() }

            Button(
                onClick = onSelectFileButtonClick,
                modifier = Modifier
                    .pointerHoverIcon(icon = PointerIcon.Hand)
                    .focusRequester(focusRequester = focusRequester)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(Res.string.dialog_unstore_item_button_file_text))
                    Icon(imageVector = Icons.Default.FileOpen, contentDescription = null)
                }
            }

            if (fileChooserDialogResult != null) {
                if (fileChooserDialogResult is FileChooserResult.None) {
                    Text(
                        text = stringResource(Res.string.dialog_unstore_item_file_error),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                } else {
                    val file = fileChooserDialogResult as FileChooserResult.File

                    Text(text = FileSelected(file.path, file.bytes).fileName, fontSize = 14.sp)
                }
            }

            LaunchedEffect(focusRequester) {
                focusRequester.requestFocus()
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