package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Chain

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainListItemDraft(chain: Chain, onIconDoneClick: () -> Unit, onIconClearClick: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

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
        Row(
            modifier = Modifier.fillMaxWidth().padding(all = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onDone
            ) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
            IconButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onIconClearClick
            ) { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
        }
        TextField(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).focusRequester(focusRequester),
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
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        TextField(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            keyboardActions = KeyboardActions(onDone = { onDone() })
        )
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
