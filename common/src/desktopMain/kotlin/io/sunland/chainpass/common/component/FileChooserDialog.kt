package io.sunland.chainpass.common.component

import androidx.compose.runtime.Composable
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun FileChooserDialog(isOpened: Boolean, fileExtensions: List<String>, onClose: (String) -> Unit) {
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

        val filePath = if (fileDialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            fileDialog.selectedFile.absolutePath
        } else ""

        onClose(filePath)
    }
}