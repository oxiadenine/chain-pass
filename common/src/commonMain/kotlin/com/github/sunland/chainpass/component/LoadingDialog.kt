package com.github.sunland.chainpass.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.github.sunland.chainpass.Platform
import com.github.sunland.chainpass.platform

@Composable
fun LoadingDialog() = androidx.compose.ui.window.Dialog(
    onDismissRequest = {},
    properties = DialogProperties(usePlatformDefaultWidth = false)
) {
    Surface(
        shape = if (platform == Platform.DESKTOP) {
            RectangleShape
        } else MaterialTheme.shapes.large,
        tonalElevation = 4.dp
    ) {
        Box(modifier = Modifier.wrapContentSize().padding(all = 24.dp)) {
            CircularProgressIndicator(
                modifier = Modifier.align(alignment = Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}