package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkListTopBar(
    onBack: () -> Unit,
    onSync: () -> Unit,
    onAdd: () -> Unit,
    onSearch: () -> Unit,
    onStore: () -> Unit,
    onUnstore: () -> Unit
) {
    val isActionMenuExpandedState = remember { mutableStateOf(false) }

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = { Text("Chain Links") },
        navigationIcon = {
            IconButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onBack
            ) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null) }
        },
        actions = {
            IconButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = { isActionMenuExpandedState.value = true }
            ) { Icon(imageVector = Icons.Default.MoreVert, contentDescription = null) }
            DropdownMenu(
                modifier = Modifier.width(width = 150.dp),
                expanded = isActionMenuExpandedState.value,
                onDismissRequest = { isActionMenuExpandedState.value = false },
                offset = DpOffset(x = 4.dp, y = (-48).dp)
            ) {
                DropdownMenuItem(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = {
                        isActionMenuExpandedState.value = false

                        onSync()
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null)
                        Text(text = "Sync", fontSize = 12.sp)
                    }
                }
                DropdownMenuItem(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = {
                        isActionMenuExpandedState.value = false

                        onAdd()
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Text(text = "Add", fontSize = 12.sp)
                    }
                }
                DropdownMenuItem(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = {
                        isActionMenuExpandedState.value = false

                        onSearch()
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        Text(text = "Search", fontSize = 12.sp)
                    }
                }
                DropdownMenuItem(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = {
                        isActionMenuExpandedState.value = false

                        onStore()
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Archive, contentDescription = null)
                        Text(text = "Store", fontSize = 12.sp)
                    }
                }
                DropdownMenuItem(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = {
                        isActionMenuExpandedState.value = false

                        onUnstore()
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Unarchive, contentDescription = null)
                        Text(text = "Unstore", fontSize = 12.sp)
                    }
                }
            }
        }
    )
}