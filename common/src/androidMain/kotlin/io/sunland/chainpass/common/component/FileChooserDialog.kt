package io.sunland.chainpass.common.component

import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
actual fun FileChooserDialog(isOpened: Boolean, fileExtensions: List<String>, onClose: (String) -> Unit) {
    val resultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val filePath = uri?.path?.let { path ->
            "${Environment.getExternalStorageDirectory().absolutePath}/${path.substringAfterLast(":")}"
        } ?: ""

        onClose(filePath)
    }

    if (isOpened) {
        resultLauncher.launch(fileExtensions.toTypedArray())
    }
}