package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.ChainLink

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkListItem(chainLink: ChainLink, onIconEditClick: () -> Unit, onIconDeleteClick: () -> Unit) {
    val passwordVisibleState = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(modifier = Modifier.padding(all = 16.dp), text = chainLink.name.value)
            Row {
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = onIconEditClick
                ) { Icon(imageVector = Icons.Default.Edit, contentDescription = null) }
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = onIconDeleteClick
                ) { Icon(imageVector = Icons.Default.Delete, contentDescription = null) }
            }
        }
        if (chainLink.description.value.isNotEmpty()) {
            Text(modifier = Modifier.padding(all = 16.dp), text = chainLink.description.value)
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
