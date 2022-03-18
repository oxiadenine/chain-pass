package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.security.GeneratorSpec
import io.sunland.chainpass.common.security.PasswordGenerator

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkListItemDraft(chainLink: ChainLink, onIconDoneClick: () -> Unit, onIconClearClick: () -> Unit) {
    val passwordGenerator = PasswordGenerator(GeneratorSpec.Strength(16))

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val nameState = mutableStateOf(chainLink.name.value)
    val nameErrorState = mutableStateOf(!chainLink.name.isValid)

    val descriptionState = mutableStateOf(chainLink.description.value)
    val descriptionErrorState = mutableStateOf(!chainLink.description.isValid)

    val passwordState = mutableStateOf(chainLink.password.value)
    val passwordErrorState = mutableStateOf(!chainLink.password.isValid)

    val onNameChange = { value: String ->
        chainLink.name = ChainLink.Name(value)

        nameState.value = chainLink.name.value
        nameErrorState.value = !chainLink.name.isValid
    }

    val onDescriptionChange = { value: String ->
        chainLink.description = ChainLink.Description(value)

        descriptionState.value = chainLink.description.value
        descriptionErrorState.value = !chainLink.description.isValid
    }

    val onPasswordChange = { value: String ->
        chainLink.password = ChainLink.Password(value)

        passwordState.value = chainLink.password.value
        passwordErrorState.value = !chainLink.password.isValid
    }

    val onDone = {
        chainLink.name = ChainLink.Name(nameState.value)
        chainLink.description = ChainLink.Description(descriptionState.value)
        chainLink.password = ChainLink.Password(passwordState.value)

        nameErrorState.value = !chainLink.name.isValid
        descriptionErrorState.value = !chainLink.description.isValid
        passwordErrorState.value = !chainLink.password.isValid

        if (!nameErrorState.value && !descriptionErrorState.value && !passwordErrorState.value) {
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
            placeholder = { Text(text = "Description", fontSize = 14.sp) },
            value = descriptionState.value,
            onValueChange = onDescriptionChange,
            trailingIcon = if (descriptionErrorState.value) {
                { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
            } else null,
            isError = descriptionErrorState.value,
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp),
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
            placeholder = { Text(text = "Password") },
            value = passwordState.value,
            onValueChange = onPasswordChange,
            leadingIcon = {
                IconButton(
                    modifier = Modifier.padding(horizontal = 2.dp).pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = { onPasswordChange(passwordGenerator.generate()) }
                ) { Icon(imageVector = Icons.Default.Build, contentDescription = null) }
            },
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
            keyboardActions = KeyboardActions(onDone = { onDone() })
        )
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
