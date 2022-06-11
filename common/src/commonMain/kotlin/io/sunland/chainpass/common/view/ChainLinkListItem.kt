package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
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
fun ChainLinkListItem(
    chainLink: ChainLink,
    onIconEditClick: () -> Unit,
    onIconDeleteClick: () -> Unit,
    onIconLockClick: (Boolean) -> Unit
) {
    val passwordLockState = mutableStateOf(chainLink.password.isPrivate)

    val onLock = {
        onIconLockClick(passwordLockState.value)

        passwordLockState.value = !passwordLockState.value
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(modifier = Modifier.padding(horizontal = 16.dp), text = chainLink.name.value)
            Row(
                modifier = Modifier.padding(all = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = {
                        if (!passwordLockState.value) {
                            onLock()
                        }

                        onIconEditClick()
                    }
                ) { Icon(imageVector = Icons.Default.Edit, contentDescription = null) }
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = {
                        if (!passwordLockState.value) {
                            onLock()
                        }

                        onIconDeleteClick()
                    }
                ) { Icon(imageVector = Icons.Default.Delete, contentDescription = null) }
            }
        }
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
            SelectionContainer(modifier = Modifier.padding(horizontal = 16.dp)) {
                if (passwordLockState.value) {
                    DisableSelection { Text(text = "???", fontStyle = FontStyle.Italic) }
                } else Text(text = chainLink.password.value)
            }
            IconButton(
                modifier = Modifier.padding(horizontal = 4.dp).pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onLock
            ) { Icon(imageVector = Icons.Default.Lock, contentDescription = null) }
        }
    }
}