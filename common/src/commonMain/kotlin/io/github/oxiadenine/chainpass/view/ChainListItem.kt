package io.github.oxiadenine.chainpass.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.oxiadenine.chainpass.LocalIntl

enum class ChainListItemMenuItem { OPEN, DELETE }

@Composable
fun ChainListItem(onMenuItemClick: (ChainListItemMenuItem) -> Unit, name: String) {
    val intl = LocalIntl.current

    var dropdownMenuExpanded by remember {  mutableStateOf(false) }
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
                }.padding(vertical = 18.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) { Text(text = name) }

        if (dropdownMenuExpanded) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { dropdownMenuExpanded = false },
                modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                offset = dropdownMenuOffset
            ) {
                DropdownMenuItem(
                    text = { Text(text = intl.translate("list.chain.menu.item.open.text"), fontSize = 14.sp) },
                    onClick = {
                        dropdownMenuExpanded = false

                        onMenuItemClick(ChainListItemMenuItem.OPEN)
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                    leadingIcon = { Icon(imageVector = Icons.Default.LockOpen, contentDescription = null) },
                    trailingIcon = { Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null) },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                )
                DropdownMenuItem(
                    text = { Text(text = intl.translate("list.chain.menu.item.delete.text"), fontSize = 14.sp) },
                    onClick = {
                        dropdownMenuExpanded = false

                        onMenuItemClick(ChainListItemMenuItem.DELETE)
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                    leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null) },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                )
            }
        }
    }
}