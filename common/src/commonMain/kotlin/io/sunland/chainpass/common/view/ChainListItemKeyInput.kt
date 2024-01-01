package io.sunland.chainpass.common.view

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.component.InputDialog

@Composable
fun ChainListItemKeyInput(onDismiss: () -> Unit, onConfirm: (Chain.Key) -> Unit) {
    val keyState = remember { mutableStateOf("") }
    val keyErrorState = remember { mutableStateOf(false) }

    InputDialog(
        placeholder = { Text(text = "Key") },
        value = keyState.value,
        onValueChange = { value ->
            val chainKey = Chain.Key(value)

            keyState.value = chainKey.value
            keyErrorState.value = chainKey.value.isEmpty()
        },
        visualTransformation = PasswordVisualTransformation(),
        isError = keyErrorState.value,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        onDismissRequest = onDismiss,
        onConfirmRequest = {
            val chainKey = Chain.Key(keyState.value)

            keyErrorState.value = chainKey.value.isEmpty()

            if (!keyErrorState.value) {
                onConfirm(chainKey)
            }
        }
    )
}