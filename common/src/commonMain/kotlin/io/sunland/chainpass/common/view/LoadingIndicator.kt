package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Theme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LoadingIndicator() = AlertDialog(
    onDismissRequest = {},
    text = {
        Surface(modifier = Modifier.padding(bottom = 8.dp, start = 8.dp, end = 8.dp)) {
            CircularProgressIndicator(color = Theme.Palette.QUARTZ.color)
        }
    },
    buttons = {}
)