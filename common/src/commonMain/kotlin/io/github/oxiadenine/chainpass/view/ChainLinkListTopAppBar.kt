package io.github.oxiadenine.chainpass.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.oxiadenine.common.generated.resources.*
import io.github.oxiadenine.common.generated.resources.Res
import io.github.oxiadenine.common.generated.resources.topAppBar_chainLink_menu_item_store_text
import io.github.oxiadenine.common.generated.resources.topAppBar_chainLink_menu_item_sync_text
import org.jetbrains.compose.resources.stringResource

enum class ChainLinkListTopAppBarMenuItem { SYNC, STORE, UNSTORE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChainLinkListTopAppBar(
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMenuItemClick: (ChainLinkListTopAppBarMenuItem) -> Unit,
    title: String
) {
    var dropdownMenuExpanded by rememberSaveable { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = title) },
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
            ) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
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
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp
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

                            onMenuItemClick(ChainLinkListTopAppBarMenuItem.SYNC)
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

                            onMenuItemClick(ChainLinkListTopAppBarMenuItem.STORE)
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

                            onMenuItemClick(ChainLinkListTopAppBarMenuItem.UNSTORE)
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