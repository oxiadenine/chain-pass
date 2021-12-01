package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.ChainLink

@Composable
fun ChainLinkListItemEdit(chainLink: ChainLink, onIconDoneClick: () -> Unit, onIconClearClick: () -> Unit) {
    val passwordState = mutableStateOf(chainLink.password.value)

    val newPasswordState = mutableStateOf(chainLink.password.value)
    val newPasswordErrorState = mutableStateOf(!chainLink.password.isValid)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(modifier = Modifier.padding(all = 16.dp), text = chainLink.name.value)
            Row {
                IconButton(onClick = {
                    chainLink.password = ChainLink.Password(newPasswordState.value)

                    newPasswordErrorState.value = !chainLink.password.isValid

                    if (!newPasswordErrorState.value) {
                        onIconDoneClick()
                    }
                }) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
                IconButton(onClick = {
                    chainLink.password = ChainLink.Password(passwordState.value)

                    onIconClearClick()
                }) { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
            }
        }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Password") },
            value = newPasswordState.value,
            onValueChange = { password ->
                chainLink.password = ChainLink.Password(password)

                newPasswordState.value = chainLink.password.value
                newPasswordErrorState.value = !chainLink.password.isValid
            },
            trailingIcon = if (newPasswordErrorState.value) {
                { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
            } else null,
            isError = newPasswordErrorState.value,
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            )
        )
    }
}
