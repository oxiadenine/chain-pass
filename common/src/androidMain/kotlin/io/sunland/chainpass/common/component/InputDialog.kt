package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun InputDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
    title: String?,
    content: @Composable (() -> Unit)?
) = Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
    Surface {
        Column(
            modifier = Modifier.widthIn(min = 200.dp, max = 300.dp).padding(horizontal = 16.dp).padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
        ) {
            if (!title.isNullOrEmpty()) {
                Text(modifier = Modifier.padding(horizontal = 16.dp), text = title)
                Column(modifier = Modifier.padding(all = 16.dp)) { content?.invoke() }
            } else Column(modifier = Modifier.padding(horizontal = 16.dp)) { content?.invoke() }

            Row(
                modifier = Modifier.fillMaxWidth().align(alignment = Alignment.End).padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismissRequest) { Text(text = "Cancel") }
                TextButton(onClick = onConfirmRequest) { Text(text = "Ok") }
            }
        }
    }
}