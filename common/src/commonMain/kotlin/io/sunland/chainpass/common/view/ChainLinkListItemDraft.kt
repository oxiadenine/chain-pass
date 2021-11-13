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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import io.sunland.chainpass.common.ChainLink

@Composable
fun ChainLinkListItemDraft(chainLink: ChainLink, onIconDoneClick: () -> Unit, onIconClearClick: () -> Unit) {
    val nameState = remember { mutableStateOf("") }
    val nameErrorState = remember { mutableStateOf(false) }

    val passwordState = remember { mutableStateOf("") }
    val passwordErrorState = remember { mutableStateOf(false) }

    nameState.value = chainLink.name
    passwordState.value = chainLink.password

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = {
                runCatching { chainLink.name = nameState.value }
                    .onSuccess { nameErrorState.value = false }
                    .onFailure { nameErrorState.value = true }

                runCatching { chainLink.password = passwordState.value }
                    .onSuccess { passwordErrorState.value = false }
                    .onFailure { passwordErrorState.value = true }

                if (!nameErrorState.value && !passwordErrorState.value) {
                    onIconDoneClick()
                }
            }) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
            IconButton(onClick = {
                nameState.value = chainLink.name
                passwordState.value = chainLink.password

                onIconClearClick()
            }) { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
        }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Name") },
            value = nameState.value,
            onValueChange = { name ->
                nameState.value = name

                runCatching { chainLink.name = nameState.value }
                    .onSuccess { nameErrorState.value = false }
                    .onFailure { nameErrorState.value = true }
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
            placeholder = { Text(text = "Key") },
            value = passwordState.value,
            onValueChange = { password ->
                passwordState.value = password

                runCatching { chainLink.password = passwordState.value }
                    .onSuccess { passwordErrorState.value = false }
                    .onFailure { passwordErrorState.value = true }
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
