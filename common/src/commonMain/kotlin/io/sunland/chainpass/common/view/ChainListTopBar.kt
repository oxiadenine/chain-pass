package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainListTopBar(
    serverAddress: ServerAddress,
    onIconExitClick: () -> Unit,
    onIconRefreshClick: () -> Unit,
    onIconAddClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Chains")
                Text(text = "(${serverAddress.host.value}:${serverAddress.port.value})", fontSize = 12.sp)
            }
        },
        actions = {
            IconButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onIconRefreshClick
            ) { Icon(imageVector = Icons.Default.Refresh, contentDescription = null) }
            IconButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onIconAddClick
            ) { Icon(imageVector = Icons.Default.Add, contentDescription = null) }
            IconButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onIconExitClick
            ) { Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null) }
        }
    )
}
