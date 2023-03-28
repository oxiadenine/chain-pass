package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChainLinkListTopBar(
    title: String,
    onBack: () -> Unit,
    onSync: () -> Unit,
    onSearch: () -> Unit,
    onStore: () -> Unit,
    onUnstore: () -> Unit
) {
    val isActionMenuExpandedState = remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = title) },
        modifier = Modifier.fillMaxWidth(),
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
                offset = DpOffset(x = 8.dp, y = (-40).dp)
            ) {
                DropdownMenuItem(
                    text = { Text(text = "Sync", fontSize = 14.sp) },
                    onClick = {
                        isActionMenuExpandedState.value = false

                        onSync()
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    leadingIcon = { Icon(imageVector = Icons.Default.Sync, contentDescription = null) },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                )
                DropdownMenuItem(
                    text = { Text(text = "Store", fontSize = 14.sp) },
                    onClick = {
                        isActionMenuExpandedState.value = false

                        onStore()
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    leadingIcon = { Icon(imageVector = Icons.Default.Archive, contentDescription = null) },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                )
                DropdownMenuItem(
                    text = { Text(text = "Unstore", fontSize = 14.sp) },
                    onClick = {
                        isActionMenuExpandedState.value = false

                        onUnstore()
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    leadingIcon = { Icon(imageVector = Icons.Default.Unarchive, contentDescription = null) },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                )
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(elevation = 4.dp)
        )
    )
}