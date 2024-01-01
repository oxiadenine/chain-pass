package io.sunland.chainpass.common.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.ChainLink

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkSearchListItem(chainLink: ChainLink, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clickable(onClick = onSelect).pointerHoverIcon(icon = PointerIconDefaults.Hand)) {
        Text(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp), text = chainLink.name.value)
        if (chainLink.description.value.isNotEmpty()) {
            Text(
                text = chainLink.description.value,
                modifier = Modifier.padding(all = 16.dp),
                fontSize = 14.sp
            )
        }
    }
}