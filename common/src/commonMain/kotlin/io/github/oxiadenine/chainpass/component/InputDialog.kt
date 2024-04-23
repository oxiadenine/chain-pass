package io.github.oxiadenine.chainpass.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.oxiadenine.common.generated.resources.Res
import io.github.oxiadenine.common.generated.resources.inputDialog_button_cancel_text
import io.github.oxiadenine.common.generated.resources.inputDialog_button_confirm_text
import org.jetbrains.compose.resources.stringResource

@Composable
fun InputDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
    title: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
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
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                ) { Text(text = stringResource(Res.string.inputDialog_button_cancel_text)) }
                TextButton(
                    onClick = onConfirmRequest,
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                ) { Text(text = stringResource(Res.string.inputDialog_button_confirm_text)) }
            }
        },
        content = content
    )
}