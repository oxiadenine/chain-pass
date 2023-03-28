package io.sunland.chainpass.common.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.ChainLink

@Composable
fun ChainLinkSearchListItem(chainLink: ChainLink, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clickable(onClick = onSelect)) {
        if (chainLink.description.value.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Text(text = chainLink.name.value)
                Text(text = chainLink.description.value, fontSize = 14.sp)
            }
        } else Text(text = chainLink.name.value, modifier = Modifier.padding(all = 16.dp))
    }
}