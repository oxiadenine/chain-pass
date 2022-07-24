package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.ChainLink

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkSearchListItem(chainLink: ChainLink, onIconLockClick: (Boolean) -> Unit) {
    val passwordLockState = mutableStateOf(chainLink.password.isPrivate)
    val passwordLockIconState = mutableStateOf(Icons.Default.Lock)

    if (passwordLockState.value) {
        passwordLockIconState.value = Icons.Default.Lock
    } else passwordLockIconState.value = Icons.Default.LockOpen

    val onLock = {
        onIconLockClick(passwordLockState.value)

        passwordLockState.value = !passwordLockState.value
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(modifier = Modifier.padding(horizontal = 16.dp, vertical = 17.dp), text = chainLink.name.value)
        if (chainLink.description.value.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                text = chainLink.description.value,
                fontSize = 14.sp
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionContainer(Modifier.padding(horizontal = 16.dp)) {
                if (passwordLockState.value) {
                    DisableSelection { Text(text = "???", fontStyle = FontStyle.Italic) }
                } else Text(text = chainLink.password.value)
            }
            IconButton(
                modifier = Modifier.padding(horizontal = 4.dp).pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onLock
            ) { Icon(imageVector = passwordLockIconState.value, contentDescription = null) }
        }
    }
}