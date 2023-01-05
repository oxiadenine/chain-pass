package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.PopupAlertDialogProvider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
actual fun Dialog(content: @Composable () -> Unit) = PopupAlertDialogProvider.AlertDialog(onDismissRequest = {}) {
    Surface(modifier = Modifier.padding(all = 24.dp)) {
        content.invoke()
    }
}