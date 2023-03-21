package io.sunland.chainpass.common.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.ChainLink

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun ChainLinkListItem(
    chainLink: ChainLink,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onPasswordCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDropDownMenuExpandedState = remember { mutableStateOf(false) }
    val dropDownMenuOffsetState = remember { mutableStateOf(DpOffset.Zero) }

    val density = LocalDensity.current

    Column(
        modifier = modifier
            .clickable { isDropDownMenuExpandedState.value = true }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent().changes.first()

                        if (event.pressed && (event.type == PointerType.Mouse || event.type == PointerType.Touch)) {
                            dropDownMenuOffsetState.value = with(density) {
                                DpOffset(
                                    x = event.position.x.toDp(),
                                    y = event.position.y.toDp() - 48.dp
                                )
                            }
                        }
                    }
                }
            }
    ) {
        if (chainLink.description.value.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Text(text = chainLink.name.value)
                Text(text = chainLink.description.value, fontSize = 14.sp)
            }
        } else Text(text = chainLink.name.value, modifier = Modifier.padding(all = 16.dp))

        if (isDropDownMenuExpandedState.value) {
            DropdownMenu(
                expanded = isDropDownMenuExpandedState.value,
                onDismissRequest = { isDropDownMenuExpandedState.value = false },
                modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                offset = dropDownMenuOffsetState.value
            ) {
                DropdownMenuItem(
                    onClick = {
                        isDropDownMenuExpandedState.value = false

                        onEdit()
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                        Text(text = "Edit", fontSize = 12.sp)
                    }
                }
                DropdownMenuItem(
                    onClick = {
                        isDropDownMenuExpandedState.value = false

                        onRemove()
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Text(text = "Delete", fontSize = 12.sp)
                    }
                }
                DropdownMenuItem(
                    onClick = {
                        isDropDownMenuExpandedState.value = false

                        onPasswordCopy()
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CopyAll, contentDescription = null)
                        Text(text = "Copy", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}