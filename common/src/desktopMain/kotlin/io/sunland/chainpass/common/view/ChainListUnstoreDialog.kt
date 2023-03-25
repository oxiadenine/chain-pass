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
actual fun ChainListUnstoreDialog(isSingle: Boolean, onUnstore: (FilePath) -> Unit, onCancel: () -> Unit) {
    val fileDialogOpenState = remember { mutableStateOf(false) }

    val filePathState = remember { mutableStateOf("") }
    val filePathErrorState = remember { mutableStateOf(false) }

    val onDone = {
        val filePath = FilePath(filePathState.value)

        filePathErrorState.value = filePath.value.isEmpty()

        if (!filePathErrorState.value) {
            onUnstore(filePath)
        }
    }

    InputDialog(
        onDismissRequest = onCancel,
        onConfirmRequest = onDone,
        title = {
            Text(
                text = if (isSingle) "Single Unstore" else "Multiple Unstore",
                modifier = Modifier.padding(all = 16.dp)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Button(
                onClick = { fileDialogOpenState.value = true },
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
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
    }

    if (fileDialogOpenState.value) {
        val fileExtensionFilters = mutableListOf(FileNameExtensionFilter(StorageType.JSON.name, StorageType.JSON.name))

        if (isSingle) {
            fileExtensionFilters.add(FileNameExtensionFilter(StorageType.CSV.name, StorageType.CSV.name))
        }

        val fileDialog = JFileChooser().apply {
            dialogType = JFileChooser.OPEN_DIALOG
            fileSelectionMode = JFileChooser.FILES_ONLY
            isAcceptAllFileFilterUsed = false

            fileExtensionFilters.forEach { fileExtensionFilter ->
                addChoosableFileFilter(fileExtensionFilter)
            }
        }

        if (fileDialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            filePathState.value = fileDialog.selectedFile.absolutePath
        } else filePathState.value = ""

        filePathErrorState.value = filePathState.value.isEmpty()

        fileDialogOpenState.value = false
    }
}