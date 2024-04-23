package io.github.oxiadenine.chainpass.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.oxiadenine.common.generated.resources.*
import io.github.oxiadenine.common.generated.resources.Res
import io.github.oxiadenine.common.generated.resources.topAppBar_chainLink_menu_item_store_text
import io.github.oxiadenine.common.generated.resources.topAppBar_chainLink_menu_item_sync_text
import org.jetbrains.compose.resources.stringResource

enum class ChainListTopAppBarMenuItem { SYNC, STORE, UNSTORE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChainListTopAppBar(
    onMenuClick: () -> Unit,
    onMenuItemClick: (ChainListTopAppBarMenuItem) -> Unit,
    title: String
) {
    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = title) },
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
            ) { Icon(imageVector = Icons.Default.Menu, contentDescription = null) }
        },
        actions = {
            IconButton(
                onClick = { dropdownMenuExpanded = true },
                modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
            ) { Icon(imageVector = Icons.Default.MoreVert, contentDescription = null) }

            if (dropdownMenuExpanded) {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { dropdownMenuExpanded = false },
                    modifier = Modifier.width(width = 150.dp),
                    offset = DpOffset(x = 8.dp, y = (-40).dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.topAppBar_chainLink_menu_item_sync_text),
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            dropdownMenuExpanded = false

                            onMenuItemClick(ChainListTopAppBarMenuItem.SYNC)
                        },
                        modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                        leadingIcon = { Icon(imageVector = Icons.Default.Sync, contentDescription = null) },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.topAppBar_chainLink_menu_item_store_text),
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            dropdownMenuExpanded = false

                            onMenuItemClick(ChainListTopAppBarMenuItem.STORE)
                        },
                        modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                        leadingIcon = { Icon(imageVector = Icons.Default.Archive, contentDescription = null) },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.topAppBar_chainLink_menu_item_unstore_text),
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            dropdownMenuExpanded = false

                            onMenuItemClick(ChainListTopAppBarMenuItem.UNSTORE)
                        },
                        modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                        leadingIcon = { Icon(imageVector = Icons.Default.Unarchive, contentDescription = null) },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(elevation = 4.dp)
        )
    )
}