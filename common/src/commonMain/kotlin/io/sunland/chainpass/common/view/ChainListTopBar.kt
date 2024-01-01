package io.sunland.chainpass.common.view

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable

@Composable
fun ChainListTopBar(onIconAddClick: () -> Unit, onIconRefreshClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = "Chains") },
        actions = {
            IconButton(onClick = onIconRefreshClick) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            }
            IconButton(onClick = onIconAddClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    )
}
