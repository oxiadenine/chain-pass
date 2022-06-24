package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkSearchListTopBar(onIconArrowBackClick: () -> Unit, onSearch: (String) -> Unit) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            val focusRequester = FocusRequester()

            val keywordState = remember { mutableStateOf("") }

            val onSearchChange = { value: String ->
                keywordState.value = value

                onSearch(keywordState.value)
            }

            val onClear = {
                keywordState.value = ""

                onSearch(keywordState.value)
            }

            TextField(
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Escape) {
                        onIconArrowBackClick()

                        true
                    } else false
                },
                placeholder = { Text(text = "Search") },
                value = keywordState.value,
                onValueChange = onSearchChange,
                trailingIcon = if (keywordState.value.isNotEmpty()) {
                    {
                        IconButton(
                            modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                            onClick = onClear
                        ) { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
                    }
                } else null,
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(keywordState.value) })
            )

            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        },
        navigationIcon = {
            IconButton(
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                onClick = onIconArrowBackClick
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary.let { color ->
                        Color(color.red, color.green, color.blue, color.alpha / 2)
                    }
                )
            }
        }
    )
}