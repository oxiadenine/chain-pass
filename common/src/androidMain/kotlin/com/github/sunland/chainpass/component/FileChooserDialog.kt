package com.github.sunland.chainpass.component

import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun FileChooserDialog(
    isOpened: Boolean,
    fileExtensions: List<String>,
    onClose: (FileChooserResult) -> Unit
) {
    val context = LocalContext.current

    val resultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val result = if (uri != null) {
            context.contentResolver.openInputStream(uri)!!.use { inputStream ->
                FileChooserResult.File(
                    "${Environment.getExternalStorageDirectory().absolutePath}/${uri.path!!.substringAfterLast(":")}",
                    inputStream.readBytes()
                )
            }
        } else FileChooserResult.None

        onClose(result)
    }

    if (isOpened) {
        resultLauncher.launch(fileExtensions.toTypedArray())
    }
}