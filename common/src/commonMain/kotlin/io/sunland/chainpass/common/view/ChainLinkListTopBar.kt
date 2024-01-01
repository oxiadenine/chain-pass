package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
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
import io.sunland.chainpass.common.component.DropdownMenu
import io.sunland.chainpass.common.component.DropdownMenuItem

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkListTopBar(
    onIconArrowBackClick: () -> Unit,
    onIconRefreshClick: () -> Unit,
    onIconAddClick: () -> Unit,
    onIconSearchClick: () -> Unit
) {
    val actionMenuExpandState = remember { mutableStateOf(false) }

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = { Text("Chain Links") },
        navigationIcon = {
            IconButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onIconArrowBackClick
            ) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null) }
        },
        actions = {
            IconButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = { actionMenuExpandState.value = true }
            ) { Icon(imageVector = Icons.Default.MoreVert, contentDescription = null) }
            DropdownMenu(
                modifier = Modifier.width(width = 150.dp),
                expanded = actionMenuExpandState.value,
                onDismissRequest = { actionMenuExpandState.value = false },
                offset = DpOffset(x = 4.dp, y = (-48).dp)
            ) {
                DropdownMenuItem(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = {
                        actionMenuExpandState.value = false

                        onIconRefreshClick()
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Text(text = "Refresh", fontSize = 12.sp)
                    }
                }
                DropdownMenuItem(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = {
                        actionMenuExpandState.value = false

                        onIconAddClick()
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
                        actionMenuExpandState.value = false

                        onIconSearchClick()
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
            }
        }
    )
}