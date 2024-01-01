package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.ChainLink

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkSearchListItem(chainLink: ChainLink) {
    val passwordVisibleState = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(modifier = Modifier.padding(all = 16.dp), text = chainLink.name.value)
        if (chainLink.description.value.isNotEmpty()) {
            Text(modifier = Modifier.padding(all = 16.dp), text = chainLink.description.value, fontSize = 14.sp)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SelectionContainer(Modifier.padding(all = 16.dp)) {
                if (passwordVisibleState.value) {
                    Text(text = chainLink.password.value)
                } else DisableSelection {
                    Text(text = chainLink.password.value.toList().joinToString(separator = "") { "*" })
                }
            }
            IconButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = { passwordVisibleState.value = !passwordVisibleState.value }
            ) { Icon(imageVector = Icons.Default.Lock, contentDescription = null) }
        }
    }
}
