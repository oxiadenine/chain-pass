package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
actual fun Dialog(content: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        text = {
            Surface(modifier = Modifier.padding(bottom = 8.dp, start = 8.dp, end = 8.dp)) {
                content.invoke()
            }
        },
        buttons = {}
    )
}