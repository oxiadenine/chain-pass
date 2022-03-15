package io.sunland.chainpass.common.view

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.component.InputDialog

@Composable
fun ChainListItemKeyInput(onInputDismiss: () -> Unit, onInputConfirm: (Chain.Key) -> Unit) {
    val keyState = remember { mutableStateOf("") }
    val keyErrorState = remember { mutableStateOf(false) }

    InputDialog(
        title = null,
        placeholder = "Key",
        value = keyState.value,
        ontValueChange = { value ->
            val chainKey = Chain.Key(value)

            keyState.value = chainKey.value
            keyErrorState.value = !chainKey.isValid
        },
        visualTransformation = PasswordVisualTransformation(),
        isError = keyErrorState.value,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        onDismissRequest = onInputDismiss,
        onConfirmRequest = {
            val chainKey = Chain.Key(keyState.value)

            keyErrorState.value = !chainKey.isValid

            if (!keyErrorState.value) {
                onInputConfirm(chainKey)
            }
        }
    )
}
