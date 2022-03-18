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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.ChainLink

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkListItemEdit(chainLink: ChainLink, onIconDoneClick: () -> Unit, onIconClearClick: () -> Unit) {
    val focusRequester = remember { FocusRequester() }

    val passwordState = remember { mutableStateOf(chainLink.password.value) }
    val passwordErrorState = remember { mutableStateOf(!chainLink.password.isValid) }

    val onPasswordChange = { value: String ->
        val chainLinkPassword = ChainLink.Password(value)

        passwordState.value = chainLinkPassword.value
        passwordErrorState.value = !chainLinkPassword.isValid
    }

    val onDone = {
        val chainLinkPassword = ChainLink.Password(passwordState.value)

        passwordErrorState.value = !chainLinkPassword.isValid

        if (!passwordErrorState.value) {
            chainLink.password = chainLinkPassword

            onIconDoneClick()
        }
    }

    val onClear = {
        passwordState.value = chainLink.password.value
        passwordErrorState.value = !chainLink.password.isValid

        onIconClearClick()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(all = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(modifier = Modifier.padding(start = 14.dp), text = chainLink.name.value)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (passwordState.value != chainLink.password.value) {
                    IconButton(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = onDone
                    ) { Icon(imageVector = Icons.Default.Done, contentDescription = null) }
                }
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = onClear
                ) { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
            }
        }
        TextField(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).focusRequester(focusRequester),
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            keyboardActions = KeyboardActions(onDone = {
                if (passwordState.value != chainLink.password.value) {
                    onDone()
                }
            })
        )
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
