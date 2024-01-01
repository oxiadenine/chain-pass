package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.LocalIntl

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
    title: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val intl = LocalIntl.current

    Dialog(
        onDismissRequest = onDismissRequest,
        title = title,
        buttons = {
            Row(
                modifier = Modifier.align(alignment = Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
                ) { Text(text = intl.translate("inputDialog.button.cancel.text")) }
                TextButton(
                    onClick = onConfirmRequest,
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
                ) { Text(text = intl.translate("inputDialog.button.confirm.text")) }
            }
        },
        content = content
    )
}