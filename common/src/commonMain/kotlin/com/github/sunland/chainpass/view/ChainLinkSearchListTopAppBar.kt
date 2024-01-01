package com.github.sunland.chainpass.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import com.github.sunland.chainpass.LocalIntl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChainLinkSearchListTopAppBar(onBackClick: () -> Unit, onKeywordChange: (String) -> Unit) {
    val intl = LocalIntl.current

    val keywordState = remember { mutableStateOf("") }

    val onSearchTextFieldValueChange = { keyword: String ->
        keywordState.value = keyword

        onKeywordChange(keywordState.value)
    }

    val onClearIconButtonClick = {
        keywordState.value = ""

        onKeywordChange(keywordState.value)
    }

    TopAppBar(
        title = {
            val focusRequester = remember { FocusRequester() }

            TextField(
                value = keywordState.value,
                onValueChange = onSearchTextFieldValueChange,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester = focusRequester),
                textStyle = TextStyle(fontSize = 18.sp),
                placeholder = {
                    Text(text = intl.translate("topAppBar.chainLink.textField.search.placeholder"))
                },
                trailingIcon = if (keywordState.value.isNotEmpty()) {
                    {
                        IconButton(
                            onClick = onClearIconButtonClick,
                            modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                        ) { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
                    }
                } else null,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onKeywordChange(keywordState.value) })
            )

            LaunchedEffect(focusRequester) {
                focusRequester.requestFocus()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.let { color -> color.copy(alpha = color.alpha / 2) }
                )
            }
        }
    )
}