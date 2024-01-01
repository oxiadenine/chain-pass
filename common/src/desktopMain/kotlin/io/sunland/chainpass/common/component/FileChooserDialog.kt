package io.sunland.chainpass.common.component

import androidx.compose.runtime.Composable
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun FileChooserDialog(
    isOpened: Boolean,
    fileExtensions: List<String>,
    onClose: (FileChooserResult) -> Unit
) {
    val fileExtensionFilters = fileExtensions.map { fileExtension ->
        FileNameExtensionFilter(fileExtension, fileExtension)
    }

    if (isOpened) {
        val fileDialog = JFileChooser().apply {
            dialogType = JFileChooser.OPEN_DIALOG
            fileSelectionMode = JFileChooser.FILES_ONLY
            isAcceptAllFileFilterUsed = false

            fileExtensionFilters.forEach { fileExtensionFilter ->
                addChoosableFileFilter(fileExtensionFilter)
            }
        }

        val result = if (fileDialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            FileChooserResult.File(fileDialog.selectedFile.absolutePath, fileDialog.selectedFile.readBytes())
        } else FileChooserResult.None

        onClose(result)
    }
}