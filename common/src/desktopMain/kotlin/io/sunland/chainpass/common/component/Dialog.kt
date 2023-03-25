package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun Dialog(
    onDismissRequest: () -> Unit,
    title: @Composable (() -> Unit)?,
    buttons: @Composable (ColumnScope.() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) = PopupAlertDialogProvider.AlertDialog(onDismissRequest = onDismissRequest) {
    Column(modifier = Modifier
        .widthIn(min = 200.dp, max = 300.dp)
        .heightIn(min = 100.dp, max = 400.dp)
        .padding(top = 6.dp, start = 6.dp, end = 6.dp)
    ) {
        title?.invoke()
        Column(modifier = Modifier.weight(1f, fill = false)) {
            content.invoke(this)
        }
        buttons?.invoke(this)
    }
}