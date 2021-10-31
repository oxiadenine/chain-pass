package io.sunland.chainpass.common.view

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable

@Composable
fun CHainLinkListTopBar(onIconArrowBackClick: () -> Unit, onIconAddClick: () -> Unit) {
    TopAppBar(
        title = { Text("Chain Links") },
        navigationIcon = {
            IconButton(onClick = onIconArrowBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onIconAddClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    )
}
