package io.sunland.chainpass.common.view

import androidx.compose.runtime.Composable

class FilePath(value: String? = null) {
    var value = value ?: ""
        private set

    val fileName = value?.substringAfterLast("/")?.substringBeforeLast(".") ?: ""
}

@Composable
expect fun ChainListUnstoreInput(isSingle: Boolean, onUnstore: (FilePath) -> Unit, onCancel: () -> Unit)