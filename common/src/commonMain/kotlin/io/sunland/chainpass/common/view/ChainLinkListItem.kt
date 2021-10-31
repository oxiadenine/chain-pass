package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ChainLinkListItem(
    name: String,
    password: String,
    onIconEditClick: () -> Unit,
    onIconDeleteClick: () -> Unit
) {
    val passwordVisibleState = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(modifier = Modifier.padding(all = 16.dp), text = name)
            Row {
                IconButton(onClick = onIconEditClick) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                }
                IconButton(onClick = onIconDeleteClick) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (passwordVisibleState.value) {
                TextField(
                    value = password,
                    onValueChange = {},
                    readOnly = true,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            } else Text(
                modifier = Modifier.padding(all = 16.dp),
                text = password.toList().joinToString(separator = "") { "*" }
            )
            IconButton(onClick = { passwordVisibleState.value = !passwordVisibleState.value }) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null)
            }
        }
    }
}
