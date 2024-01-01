package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.PopupAlertDialogProvider.AlertDialog
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
actual fun Dialog(
    onDismissRequest: () -> Unit,
    title: @Composable (() -> Unit)?,
    buttons: @Composable (ColumnScope.() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) = AlertDialog(onDismissRequest = onDismissRequest, shape = RectangleShape, modifier = Modifier) {
    Surface(tonalElevation = 4.dp) {
        Column(modifier = Modifier
            .widthIn(min = 200.dp, max = 300.dp)
            .heightIn(min = 100.dp, max = 400.dp)
            .padding(top = 8.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
        ) {
            title?.invoke()
            Column(modifier = Modifier.weight(1f, fill = false)) {
                content.invoke(this)
            }
            buttons?.invoke(this)
        }
    }
}