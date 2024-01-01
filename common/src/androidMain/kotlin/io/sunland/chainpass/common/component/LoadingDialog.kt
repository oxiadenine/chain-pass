package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
actual fun LoadingDialog() = Dialog(
    onDismissRequest = {},
    properties = DialogProperties(usePlatformDefaultWidth = false)
) {
    Surface(shape = MaterialTheme.shapes.large, tonalElevation = 4.dp) {
        Box(modifier = Modifier.wrapContentSize().padding(all = 24.dp)) {
            CircularProgressIndicator(
                modifier = Modifier.align(alignment = Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}