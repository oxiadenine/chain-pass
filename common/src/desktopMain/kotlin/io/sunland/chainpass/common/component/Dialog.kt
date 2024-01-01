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
actual fun Dialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
    content: @Composable (() -> Unit)?
) = PopupAlertDialogProvider.AlertDialog(onDismissRequest) {
    Column(
        modifier = Modifier.width(width = 300.dp).padding(horizontal = 16.dp).padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) { content?.invoke() }
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