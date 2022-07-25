package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ServerConnectionTopBar(serverConnectionState: ServerConnectionState, onConnect: () -> Unit) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = { Text(text = "Chain Pass") },
        actions = {
            IconButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onConnect,
                enabled = serverConnectionState.discoveringState.value?.isActive != true
            ) { Icon(imageVector = Icons.Default.Login, contentDescription = null) }
        }
    )
}