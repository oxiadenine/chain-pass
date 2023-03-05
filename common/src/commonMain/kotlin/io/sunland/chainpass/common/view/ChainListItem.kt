package io.sunland.chainpass.common.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Chain

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainListItem(chain: Chain, onSelect: () -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clickable(onClick = onSelect).pointerHoverIcon(icon = PointerIconDefaults.Hand)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = chain.name.value, modifier = Modifier.padding(horizontal = 16.dp))
            Row(
                modifier = Modifier.padding(all = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
                ) { Icon(imageVector = Icons.Default.Delete, contentDescription = null) }
            }
        }
    }
}