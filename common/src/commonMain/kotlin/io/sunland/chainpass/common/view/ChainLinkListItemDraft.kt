package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import io.sunland.chainpass.common.ChainLink

@Composable
fun ChainLinkListItemDraft(chainLink: ChainLink, onIconDoneClick: () -> Unit, onIconClearClick: () -> Unit) {
    val nameState = mutableStateOf(chainLink.name.value)
    val nameErrorState = mutableStateOf(!chainLink.name.isValid)

    val passwordState = mutableStateOf(chainLink.password.value)
    val passwordErrorState = mutableStateOf(!chainLink.password.isValid)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = {
                chainLink.name = ChainLink.Name(nameState.value)
                chainLink.password = ChainLink.Password(passwordState.value)

                nameErrorState.value = !chainLink.name.isValid
                passwordErrorState.value = !chainLink.password.isValid

                if (!nameErrorState.value && !passwordErrorState.value) {
                    onIconDoneClick()
                }
            }) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
            IconButton(onClick = onIconClearClick) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = null)
            }
        }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Name") },
            value = nameState.value,
            onValueChange = { name ->
                chainLink.name = ChainLink.Name(name)

                nameState.value = chainLink.name.value
                nameErrorState.value = !chainLink.name.isValid
            },
            trailingIcon = if (nameErrorState.value) {
                { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
            } else null,
            isError = nameErrorState.value,
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            )
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Password") },
            value = passwordState.value,
            onValueChange = { password ->
                chainLink.password = ChainLink.Password(password)

                passwordState.value = chainLink.password.value
                passwordErrorState.value = !chainLink.password.isValid
            },
            trailingIcon = if (passwordErrorState.value) {
                { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordErrorState.value,
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
