package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.Platform
import io.sunland.chainpass.common.StorageType
import io.sunland.chainpass.common.component.FileChooserDialog
import io.sunland.chainpass.common.component.InputDialog
import io.sunland.chainpass.common.platform

class FilePath(value: String? = null) {
    var value = value ?: ""
        private set

    val fileName = value?.substringAfterLast("/")?.substringBeforeLast(".") ?: ""
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainListUnstoreDialog(isSingle: Boolean, onUnstore: (FilePath) -> Unit, onCancel: () -> Unit) {
    val isFileDialogOpenedState = remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { isFileDialogOpenedState.value = true },
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
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
                Text(text = "File is not selected", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }
        }
    }

    val fileExtensions = mutableListOf(if (platform == Platform.DESKTOP) {
        StorageType.JSON.name
    } else "application/${StorageType.JSON.name.lowercase()}")

    if (isSingle) {
        fileExtensions.add(if (platform == Platform.DESKTOP) {
            StorageType.CSV.name
        } else "text/comma-separated-values")
    }

    FileChooserDialog(
        isOpened = isFileDialogOpenedState.value,
        fileExtensions = fileExtensions.toList(),
        onClose = { filePath ->
            filePathState.value = filePath
            filePathErrorState.value = filePathState.value.isEmpty()

            isFileDialogOpenedState.value = false
        }
    )
}