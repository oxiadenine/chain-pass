package io.sunland.chainpass.common.view

import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.StorageType
import io.sunland.chainpass.common.component.InputDialog

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
        val resultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            filePathState.value = uri?.path?.let { path ->
                "${Environment.getExternalStorageDirectory().absolutePath}/${path.substringAfterLast(":")}"
            } ?: ""
            filePathErrorState.value = filePathState.value.isEmpty()
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            Button(
                onClick = { resultLauncher.launch(arrayOf("application/${StorageType.JSON.name.lowercase()}")) },
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
}