package io.sunland.chainpass.common.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class ScaffoldListState(
    val popupHostState: PopupHostState,
    val snackbarHostState: SnackbarHostState,
    val lazyListState: LazyListState
)

@Composable
fun rememberScaffoldListState(): ScaffoldListState {
    val popupHostState = remember { PopupHostState() }
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()

    return ScaffoldListState(popupHostState, snackbarHostState, lazyListState)
}

@Composable
fun ScaffoldList(
    scaffoldListState: ScaffoldListState,
    popupHost: @Composable (PopupHostState) -> Unit = { hostState -> PopupHost(hostState) },
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { hostState -> SnackbarHost(hostState) },
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingButton: @Composable () -> Unit = {},
    floatingButtonPosition: Alignment = Alignment.BottomEnd,
    content: @Composable BoxScope.(LazyListState) -> Unit
) = Surface {
    Column(modifier = modifier) {
        topBar()

        Box(modifier = Modifier.fillMaxSize()) {
            popupHost(scaffoldListState.popupHostState)

            content(scaffoldListState.lazyListState)

            Column(modifier = Modifier.align(alignment = floatingButtonPosition)) {
                floatingButton()
            }

            Column(modifier = Modifier.align(alignment = Alignment.BottomEnd)) {
                snackbarHost(scaffoldListState.snackbarHostState)
            }
        }
    }
}