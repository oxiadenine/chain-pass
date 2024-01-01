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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ChainLinkListItemEdit(
    password: String,
    chainLinkListItem: ChainLinkListItem,
    onIconDoneClick: () -> Unit,
    onIconClearClick: () -> Unit
) {
    val passwordState = remember { mutableStateOf(chainLinkListItem.password) }
    val passwordErrorState = remember { mutableStateOf(false) }

    passwordState.value = chainLinkListItem.password

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(modifier = Modifier.padding(all = 16.dp), text = chainLinkListItem.name)
            Row {
                IconButton(onClick = {
                    passwordErrorState.value = passwordState.value.isEmpty() || passwordState.value.length > 32

                    if (!passwordErrorState.value) {
                        onIconDoneClick()
                    }
                }) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
                IconButton(onClick = {
                    chainLinkListItem.password = password

                    onIconClearClick()
                }) { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
            }
        }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Password") },
            value = passwordState.value,
            onValueChange = { password ->
                passwordState.value = password
                passwordErrorState.value = password.isEmpty() || password.length > 32

                chainLinkListItem.password = password
            },
            trailingIcon = if (passwordErrorState.value) {
                { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
            } else null,
            isError = passwordErrorState.value,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            )
        )
    }
}
