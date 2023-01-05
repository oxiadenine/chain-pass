package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
actual fun InputDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
    content: @Composable (() -> Unit)?
) {
    AlertDialog(
        modifier = Modifier.requiredWidth(width = 300.dp),
        onDismissRequest = onDismissRequest,
        title = { Text(text = "") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(space = 16.dp)
            ) { content?.invoke() }
        },
        buttons = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                Row(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                    TextButton(onClick = onDismissRequest) { Text(text = "Cancel") }
                    TextButton(onClick = onConfirmRequest) { Text(text = "Ok") }
                }
            }
        }
    )
}