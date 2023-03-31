package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ChainListTopAppBarMenuItem { SYNC, STORE, UNSTORE }

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
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
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
            ) { Icon(imageVector = Icons.Default.Menu, contentDescription = null) }
        },
        actions = {
            IconButton(
                onClick = { dropdownMenuExpanded = true },
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
            ) { Icon(imageVector = Icons.Default.MoreVert, contentDescription = null) }

            if (dropdownMenuExpanded) {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { dropdownMenuExpanded = false },
                    modifier = Modifier.width(width = 150.dp),
                    offset = DpOffset(x = 8.dp, y = (-40).dp)
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "Sync", fontSize = 14.sp) },
                        onClick = {
                            dropdownMenuExpanded = false

                            onMenuItemClick(ChainListTopAppBarMenuItem.SYNC)
                        },
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        leadingIcon = { Icon(imageVector = Icons.Default.Sync, contentDescription = null) },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Store", fontSize = 14.sp) },
                        onClick = {
                            dropdownMenuExpanded = false

                            onMenuItemClick(ChainListTopAppBarMenuItem.STORE)
                        },
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        leadingIcon = { Icon(imageVector = Icons.Default.Archive, contentDescription = null) },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    )
                    DropdownMenuItem(
                        text = { Text(text = "Unstore", fontSize = 14.sp) },
                        onClick = {
                            dropdownMenuExpanded = false

                            onMenuItemClick(ChainListTopAppBarMenuItem.UNSTORE)
                        },
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
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