package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.component.PopupText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkListItem(
    chainLink: ChainLink,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPasswordCopy: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val passwordCopiedTextVisibleState = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.padding(horizontal = 4.dp).pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = {
                        onPasswordCopy()

                        coroutineScope.launch {
                            passwordCopiedTextVisibleState.value = true

                            delay(1000L)

                            passwordCopiedTextVisibleState.value = false
                        }
                    }
                ) { Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null) }
                Text(text = chainLink.name.value)
            }
            Row(
                modifier = Modifier.padding(all = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = onEdit
                ) { Icon(imageVector = Icons.Default.Edit, contentDescription = null) }
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = onDelete
                ) { Icon(imageVector = Icons.Default.Delete, contentDescription = null) }
            }
        }

        if (chainLink.description.value.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(all = 16.dp),
                text = chainLink.description.value,
                fontSize = 14.sp
            )
        }

        if (passwordCopiedTextVisibleState.value) {
            PopupText(alignment = Alignment.Center, text = "Copied")
        }
    }
}