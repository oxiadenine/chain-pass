package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.LocalIntl

enum class ChainLinkListTopAppBarMenuItem { SYNC, STORE, UNSTORE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChainLinkListTopAppBar(
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMenuItemClick: (ChainLinkListTopAppBarMenuItem) -> Unit,
    title: String
) {
    val intl = LocalIntl.current

    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = title) },
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
            ) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null) }
        },
        actions = {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
            ) { Icon(imageVector = Icons.Default.Search, contentDescription = null) }
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
                                text = intl.translate("topAppBar.chainLink.menu.item.sync.text"),
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            dropdownMenuExpanded = false

                            onMenuItemClick(ChainLinkListTopAppBarMenuItem.SYNC)
                        },
                        modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                        leadingIcon = { Icon(imageVector = Icons.Default.Sync, contentDescription = null) },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = intl.translate("topAppBar.chainLink.menu.item.store.text"),
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            dropdownMenuExpanded = false

                            onMenuItemClick(ChainLinkListTopAppBarMenuItem.STORE)
                        },
                        modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                        leadingIcon = { Icon(imageVector = Icons.Default.Archive, contentDescription = null) },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = intl.translate("topAppBar.chainLink.menu.item.unstore.text"),
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            dropdownMenuExpanded = false

                            onMenuItemClick(ChainLinkListTopAppBarMenuItem.UNSTORE)
                        },
                        modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                        leadingIcon = { Icon(imageVector = Icons.Default.Unarchive, contentDescription = null) },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(elevation = 4.dp)
        )
    )
}