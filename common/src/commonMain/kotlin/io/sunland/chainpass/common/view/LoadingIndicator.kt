package io.sunland.chainpass.common.view

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import io.sunland.chainpass.common.Theme
import io.sunland.chainpass.common.component.Dialog

@Composable
fun LoadingIndicator() = Dialog {
    CircularProgressIndicator(color = Theme.Palette.QUARTZ.color)
}