package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup

@Composable
actual fun PopupText(alignment: Alignment, offset: IntOffset, text: String) =
    Popup(alignment = alignment, offset = offset) {
        Surface(modifier = Modifier.shadow(4.dp), elevation = 4.dp) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                text = text,
                fontSize = 12.sp
            )
        }
    }