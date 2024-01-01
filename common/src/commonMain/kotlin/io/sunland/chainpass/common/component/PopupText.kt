package io.sunland.chainpass.common.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntOffset

@Composable
expect fun PopupText(alignment: Alignment, offset: IntOffset = IntOffset.Zero, text: String)