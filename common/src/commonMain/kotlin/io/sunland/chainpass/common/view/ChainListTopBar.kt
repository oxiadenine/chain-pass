package io.sunland.chainpass.common.view

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable

@Composable
fun ChainListTopBar(onIconAddClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = "Chains") },
        actions = {
            IconButton(onClick = onIconAddClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    )
}
