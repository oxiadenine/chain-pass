package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.PopupAlertDialogProvider.AlertDialog
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
actual fun LoadingDialog() = AlertDialog(onDismissRequest = {}) {
    Surface(shape = RectangleShape, tonalElevation = 4.dp) {
        Box(modifier = Modifier.wrapContentSize().padding(all = 24.dp)) {
            CircularProgressIndicator(
                modifier = Modifier.align(alignment = Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}