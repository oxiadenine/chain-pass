package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkSearchListTopBar(onIconArrowBackClick: () -> Unit, onSearch: (String) -> Unit) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            val focusRequester = FocusRequester()

            val keywordState = remember { mutableStateOf("") }

            TextField(
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                placeholder = { Text(text = "Search") },
                value = keywordState.value,
                onValueChange = { value ->
                    keywordState.value = value

                    onSearch(keywordState.value)
                },
                trailingIcon = if (keywordState.value.isNotEmpty()) {
                    {
                        IconButton(
                            modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                            onClick = {
                                keywordState.value = ""

                                onSearch(keywordState.value)
                            }
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
                keyboardActions = KeyboardActions(onDone = { onSearch(keywordState.value) })
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
