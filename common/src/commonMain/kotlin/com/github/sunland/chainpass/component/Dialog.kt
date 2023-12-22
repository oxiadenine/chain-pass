package com.github.sunland.chainpass.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.github.sunland.chainpass.Platform
import com.github.sunland.chainpass.platform

@Composable
fun Dialog(
    onDismissRequest: () -> Unit,
    title: @Composable (() -> Unit)? = null,
    buttons: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) = androidx.compose.ui.window.Dialog(
    onDismissRequest = onDismissRequest,
    properties = DialogProperties(usePlatformDefaultWidth = false)
) {
    Surface(
        shape = if (platform == Platform.DESKTOP) {
            RectangleShape
        } else MaterialTheme.shapes.large,
        tonalElevation = 4.dp
    ) {
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