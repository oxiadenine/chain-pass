package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.component.Dialog

class FilePath(value: String? = null) {
    var value = value ?: ""
        private set

    val validation = value?.let {
        if (value.isEmpty()) {
            Result.failure(IllegalArgumentException("File path is empty"))
        } else if (!value.matches("^(?:\\w:)?(/[a-zA-Z_\\-\\s\\d.]+)+\\.(csv|json|txt)\$".toRegex())) {
            Result.failure(IllegalArgumentException("File path is not valid"))
        } else Result.success(value)
    } ?: Result.success(this.value)

    val fileName = value?.substringAfterLast("/")?.substringBeforeLast(".") ?: ""
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkListUnstoreInput(onDismiss: () -> Unit, onConfirm: (FilePath) -> Unit) {
    val focusRequester = remember { FocusRequester() }

    val filePathState = remember { mutableStateOf("") }
    val filePathErrorState = remember { mutableStateOf(false) }

    val onFilePathChange = { value: String ->
        val filePath = FilePath(value)

        filePathState.value = filePath.value
        filePathErrorState.value = filePath.validation.isFailure
    }

    val onDone = {
        val filePath = FilePath(filePathState.value)

        filePathErrorState.value = filePath.validation.isFailure

        if (!filePathErrorState.value) {
            onConfirm(filePath)
        }
    }

    val onKeyEvent = { keyEvent: KeyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown) {
            false
        } else when (keyEvent.key) {
            Key.Escape -> {
                onDismiss()

                true
            }
            Key.Enter -> {
                onDone()

                true
            }
            else -> false
        }
    }

    Dialog(onDismissRequest = onDismiss, onConfirmRequest = { onDone() }) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onKeyEvent(onKeyEvent),
                placeholder = { Text(text = "File Path") },
                value = filePathState.value,
                onValueChange = onFilePathChange,
                trailingIcon = if (filePathErrorState.value) {
                    { Icon(imageVector = Icons.Default.Info, contentDescription = null) }
                } else null,
                isError = filePathErrorState.value,
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                keyboardActions = KeyboardActions(onDone = { onDone() })
            )
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}