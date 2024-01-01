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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import io.sunland.chainpass.common.ChainLink

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkListItemDraft(chainLink: ChainLink, onIconDoneClick: () -> Unit, onIconClearClick: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val focusRequester = FocusRequester()

    val nameState = mutableStateOf(chainLink.name.value)
    val nameErrorState = mutableStateOf(!chainLink.name.isValid)

    val passwordState = mutableStateOf(chainLink.password.value)
    val passwordErrorState = mutableStateOf(!chainLink.password.isValid)

    val onNameChange = { value: String ->
        chainLink.name = ChainLink.Name(value)

        nameState.value = chainLink.name.value
        nameErrorState.value = !chainLink.name.isValid
    }

    val onPasswordChange = { value: String ->
        chainLink.password = ChainLink.Password(value)

        passwordState.value = chainLink.password.value
        passwordErrorState.value = !chainLink.password.isValid
    }

    val onDone = {
        chainLink.name = ChainLink.Name(nameState.value)
        chainLink.password = ChainLink.Password(passwordState.value)

        nameErrorState.value = !chainLink.name.isValid
        passwordErrorState.value = !chainLink.password.isValid

        if (!nameErrorState.value && !passwordErrorState.value) {
            onIconDoneClick()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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
            placeholder = { Text(text = "Password") },
            value = passwordState.value,
            onValueChange = onPasswordChange,
            trailingIcon = if (passwordErrorState.value) {
                { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
            } else null,
            isError = passwordErrorState.value,
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
