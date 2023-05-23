package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
actual fun Dialog(
    onDismissRequest: () -> Unit,
    title: @Composable (() -> Unit)?,
    buttons: @Composable (ColumnScope.() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) = Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
    Surface(shape = MaterialTheme.shapes.large, tonalElevation = 4.dp) {
        Column(
            modifier = Modifier
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