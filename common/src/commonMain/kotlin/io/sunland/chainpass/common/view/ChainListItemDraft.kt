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
import io.sunland.chainpass.common.Chain

@Composable
fun ChainListItemDraft(chain: Chain, onIconDoneClick: () -> Unit, onIconClearClick: () -> Unit) {
    val nameState = remember { mutableStateOf("") }
    val nameErrorState = remember { mutableStateOf(false) }

    val keyState = remember { mutableStateOf("") }
    val keyErrorState = remember { mutableStateOf(false) }

    nameState.value = chain.name.value
    keyState.value = chain.key.value

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = {
                chain.name = Chain.Name(nameState.value)
                chain.key = Chain.Key(keyState.value)

                nameErrorState.value = !chain.name.isValid
                keyErrorState.value = !chain.key.isValid

                if (!nameErrorState.value && !keyErrorState.value) {
                    onIconDoneClick()
                }
            }) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
            IconButton(onClick = {
                nameState.value = chain.name.value
                keyState.value = chain.key.value

                onIconClearClick()
            }) { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
        }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Name") },
            value = nameState.value,
            onValueChange = { name ->
                nameState.value = name

                chain.name = Chain.Name(nameState.value)

                nameErrorState.value = !chain.name.isValid
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
            value = keyState.value,
            onValueChange = { key ->
                keyState.value = key

                chain.key = Chain.Key(keyState.value)

                keyErrorState.value = !chain.key.isValid
            },
            trailingIcon = if (keyErrorState.value) {
                { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            isError = keyErrorState.value,
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
