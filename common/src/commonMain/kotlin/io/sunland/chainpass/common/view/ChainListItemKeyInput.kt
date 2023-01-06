package io.sunland.chainpass.common.view

import androidx.compose.runtime.Composable
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.StorageType

class FilePath(value: String? = null) {
    var value = value ?: ""
        private set

    val fileName = value?.substringAfterLast("/")?.substringBeforeLast(".") ?: ""
}

data class StoreOptions(val isPrivate: Boolean = true, val type: StorageType = StorageType.JSON)

@Composable
expect fun ChainListItemKeyInput(
    inputActionType: InputActionType,
    onDismiss: () -> Unit,
    onConfirm: (Chain.Key, StoreOptions?, FilePath?) -> Unit
)