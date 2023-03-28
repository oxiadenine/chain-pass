package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChainLinkSearchListTopBar(
    keywordState: MutableState<String>,
    onBack: () -> Unit,
    onSearch: (String) -> Unit
) {
    val onSearchChange = { keyword: String ->
        keywordState.value = keyword

        onSearch(keywordState.value)
    }

    val onClear = {
        keywordState.value = ""

        onSearch(keywordState.value)
    }

    TopAppBar(
        title = {
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(focusRequester) {
                focusRequester.requestFocus()
            }

            TextField(
                value = keywordState.value,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester = focusRequester)
                    .onKeyEvent { keyEvent: KeyEvent ->
                        if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Escape) {
                            if (keywordState.value.isEmpty()) {
                                onBack()
                            } else onClear()

                            true
                        } else false
                    },
                placeholder = { Text(text = "Search") },
                trailingIcon = if (keywordState.value.isNotEmpty()) {
                    {
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
                        ) { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
                    }
                } else null,
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(keywordState.value) })
            )
        },
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.let { color ->
                        Color(color.red, color.green, color.blue, color.alpha / 2)
                    }
                )
            }
        }
    )
}