package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import io.sunland.chainpass.common.Chain

@Composable
fun ChainListItemDraft(chain: Chain, onIconDoneClick: () -> Unit, onIconClearClick: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val focusRequester = FocusRequester()

    val nameState = mutableStateOf(chain.name.value)
    val nameErrorState = mutableStateOf(!chain.name.isValid)

    val keyState = mutableStateOf(chain.key.value)
    val keyErrorState = mutableStateOf(!chain.key.isValid)

    val onNameChange = { value: String ->
        chain.name = Chain.Name(value)

        nameState.value = chain.name.value
        nameErrorState.value = !chain.name.isValid
    }

    val onKeyChange = { value: String ->
        chain.key = Chain.Key(value)

        keyState.value = chain.key.value
        keyErrorState.value = !chain.key.isValid
    }

    val onDone = {
        chain.name = Chain.Name(nameState.value)
        chain.key = Chain.Key(keyState.value)

        nameErrorState.value = !chain.name.isValid
        keyErrorState.value = !chain.key.isValid

        if (!nameErrorState.value && !keyErrorState.value) {
            onIconDoneClick()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onDone) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
            IconButton(onClick = onIconClearClick) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = null)
            }
        }
        TextField(
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            placeholder = { Text(text = "Name") },
            value = nameState.value,
            onValueChange = onNameChange,
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
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            })
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Key") },
            value = keyState.value,
            onValueChange = onKeyChange,
            trailingIcon = if (keyErrorState.value) {
                { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
            } else null,
            isError = keyErrorState.value,
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() })
        )
    }

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose {}
    }
}
