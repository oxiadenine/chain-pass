package io.sunland.chainpass.common.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChainLinkSearchListItem(onClick: () -> Unit, name: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        if (description.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Text(text = name)
                Text(text = description, fontSize = 14.sp)
            }
        } else Text(text = name, modifier = Modifier.padding(all = 16.dp))
    }
}