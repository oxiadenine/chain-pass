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
    title: String,
    onBack: () -> Unit,
    onSync: () -> Unit,
    onSearch: () -> Unit,
    onStore: () -> Unit,
    onUnstore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActionMenuExpandedState = remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
            ) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null) }
        },
        actions = {
            IconButton(
                onClick = onSearch,
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
            ) { Icon(imageVector = Icons.Default.Search, contentDescription = null) }
            IconButton(
                onClick = { isActionMenuExpandedState.value = true },
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
            ) { Icon(imageVector = Icons.Default.MoreVert, contentDescription = null) }
            DropdownMenu(
                expanded = isActionMenuExpandedState.value,
                onDismissRequest = { isActionMenuExpandedState.value = false },
                modifier = Modifier.width(width = 150.dp),
                offset = DpOffset(x = 4.dp, y = (-48).dp)
            ) {
                DropdownMenuItem(
                    onClick = {
                        isActionMenuExpandedState.value = false

                        onSync()
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null)
                        Text(text = "Sync", fontSize = 12.sp)
                    }
                }
                DropdownMenuItem(
                    onClick = {
                        isActionMenuExpandedState.value = false

                        onStore()
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Archive, contentDescription = null)
                        Text(text = "Store", fontSize = 12.sp)
                    }
                }
                DropdownMenuItem(
                    onClick = {
                        isActionMenuExpandedState.value = false

                        onUnstore()
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
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