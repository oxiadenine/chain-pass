package io.github.oxiadenine.chainpass.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*

@Composable
fun ChainLinkListItem(onClick: () -> Unit, name: String, description: String) {
    Box {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (description.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 4.dp)
                ) {
                    Text(text = name)
                    Text(text = description, fontSize = 14.sp)
                }
            } else Text(text = name, modifier = Modifier.padding(vertical = 18.dp, horizontal = 16.dp))
        }
    }
}