package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.StorageType
import io.sunland.chainpass.common.component.InputDialog
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun ChainListUnstoreInput(onSelect: (FilePath) -> Unit, onCancel: () -> Unit) {
    val filePathState = remember { mutableStateOf("") }
    val filePathErrorState = remember { mutableStateOf(false) }

    val onDone = {
        val filePath = FilePath(filePathState.value)

        filePathErrorState.value = filePath.value.isEmpty()

        if (!filePathErrorState.value) {
            onSelect(filePath)
        }
    }

    InputDialog(onDismissRequest = onCancel, onConfirmRequest = onDone) {
        val fileDialogOpenState = remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Button(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = { fileDialogOpenState.value = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Select File")
                    Icon(imageVector = Icons.Default.FileOpen, contentDescription = null)
                }
            }
            if (filePathState.value.isNotEmpty()) {
                Text(text = FilePath(filePathState.value).fileName, fontSize = 14.sp)
            }
            if (filePathErrorState.value) {
                Text(text = "File is not selected", fontSize = 14.sp, color = MaterialTheme.colors.error)
            }
        }

        if (fileDialogOpenState.value) {
            val fileDialog = JFileChooser().apply {
                dialogType = JFileChooser.OPEN_DIALOG
                fileSelectionMode = JFileChooser.FILES_ONLY
                isAcceptAllFileFilterUsed = false

                addChoosableFileFilter(FileNameExtensionFilter(StorageType.JSON.name, StorageType.JSON.name))
            }

            if (fileDialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                filePathState.value = fileDialog.selectedFile.absolutePath
            } else filePathState.value = ""

            filePathErrorState.value = filePathState.value.isEmpty()

            fileDialogOpenState.value = false
        }
    }
}