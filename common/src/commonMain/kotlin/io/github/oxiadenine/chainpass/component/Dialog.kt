package io.github.oxiadenine.chainpass.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import io.github.oxiadenine.chainpass.LocalScreen
import io.github.oxiadenine.chainpass.Platform
import io.github.oxiadenine.chainpass.platform

@Composable
fun Dialog(
    onDismissRequest: () -> Unit,
    title: @Composable (() -> Unit)? = null,
    buttons: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) = androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
    Surface(
        shape = if (platform == Platform.DESKTOP) {
            RectangleShape
        } else MaterialTheme.shapes.large,
        tonalElevation = 4.dp
    ) {
        val screen = LocalScreen.current

        val isLandscape by derivedStateOf { screen.width > screen.height }

        val maxWidth by derivedStateOf {
            if (isLandscape) 360.dp else (screen.width.value * 0.75).dp
        }
        val maxHeight by derivedStateOf { (screen.height.value * 0.75).dp }

        Column(
            modifier = Modifier
                .sizeIn(maxWidth = maxWidth, maxHeight = maxHeight)
                .padding(top = 8.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
        ) {
            title?.invoke()
            Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                content.invoke(this)
            }
            buttons?.invoke(this)
        }
    }
}