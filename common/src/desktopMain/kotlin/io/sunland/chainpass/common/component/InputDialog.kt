package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun InputDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
    title: String?,
    content: @Composable (() -> Unit)?
) = PopupAlertDialogProvider.AlertDialog(onDismissRequest = onDismissRequest) {
    Surface {
        Column(
            modifier = Modifier.widthIn(min = 200.dp, max = 300.dp).padding(horizontal = 16.dp).padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            if (!title.isNullOrEmpty()) {
                Text(modifier = Modifier.padding(horizontal = 16.dp), text = title)
                Column(modifier = Modifier.padding(all = 16.dp)) { content?.invoke() }
            } else Column(modifier = Modifier.padding(horizontal = 16.dp)) { content?.invoke() }

            Row(
                modifier = Modifier.fillMaxWidth().align(alignment = Alignment.End).padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = onDismissRequest
                ) { Text(text = "Cancel") }
                TextButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = onConfirmRequest
                ) { Text(text = "Ok") }
            }
        }
    }
}