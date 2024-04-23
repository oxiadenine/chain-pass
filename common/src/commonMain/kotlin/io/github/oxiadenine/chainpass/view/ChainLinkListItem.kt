package io.github.oxiadenine.chainpass.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.oxiadenine.common.generated.resources.*
import io.github.oxiadenine.common.generated.resources.Res
import io.github.oxiadenine.common.generated.resources.list_chainLink_menu_item_copy_text
import io.github.oxiadenine.common.generated.resources.list_chainLink_menu_item_edit_text
import org.jetbrains.compose.resources.stringResource

enum class ChainLinkListItemMenuItem { COPY, EDIT, DELETE }

@Composable
fun ChainLinkListItem(onMenuItemClick: (ChainLinkListItemMenuItem) -> Unit, name: String, description: String) {
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    var dropdownMenuOffset by remember { mutableStateOf(DpOffset.Zero) }

    Box {
        val density = LocalDensity.current

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { dropdownMenuExpanded = true }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val pointerInputChange = awaitPointerEvent().changes.first()

                            if (pointerInputChange.pressed &&
                                (pointerInputChange.type == PointerType.Mouse ||
                                        pointerInputChange.type == PointerType.Touch)
                                ) {
                                dropdownMenuOffset = with(density) {
                                    DpOffset(
                                        x = pointerInputChange.position.x.toDp() - 4.dp,
                                        y = pointerInputChange.position.y.toDp() - 64.dp
                                    )
                                }
                            }
                        }
                    }
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (description.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 4.dp)
                ) {
                    Text(text = name)
                    Text(text = description, fontSize = 14.sp)
                }
            } else Text(text = name, modifier = Modifier.padding(vertical = 18.dp, horizontal = 16.dp))
        }

        if (dropdownMenuExpanded) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { dropdownMenuExpanded = false },
                modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                offset = dropdownMenuOffset
            ) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(Res.string.list_chainLink_menu_item_copy_text), fontSize = 14.sp) },
                    onClick = {
                        dropdownMenuExpanded = false

                        onMenuItemClick(ChainLinkListItemMenuItem.COPY)
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                    leadingIcon = { Icon(imageVector = Icons.Default.CopyAll, contentDescription = null) },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(Res.string.list_chainLink_menu_item_edit_text), fontSize = 14.sp) },
                    onClick = {
                        dropdownMenuExpanded = false

                        onMenuItemClick(ChainLinkListItemMenuItem.EDIT)
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                    leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(Res.string.list_chainLink_menu_item_delete_text), fontSize = 14.sp) },
                    onClick = {
                        dropdownMenuExpanded = false

                        onMenuItemClick(ChainLinkListItemMenuItem.DELETE)
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                    leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null) },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                )
            }
        }
    }
}