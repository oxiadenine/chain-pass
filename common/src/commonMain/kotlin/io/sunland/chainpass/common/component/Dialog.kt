package io.sunland.chainpass.common.component

import androidx.compose.runtime.Composable

@Composable
expect fun Dialog(content: @Composable () -> Unit)