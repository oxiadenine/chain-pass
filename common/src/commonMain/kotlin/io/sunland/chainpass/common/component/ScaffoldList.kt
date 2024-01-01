package io.sunland.chainpass.common.component

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

class ScaffoldListState(
    val popupHostState: PopupHostState,
    val snackbarHostState: SnackbarHostState,
    val lazyListState: LazyListState
) {
    var scaffoldListSize by mutableStateOf(DpSize.Zero)
    var snackbarSize by mutableStateOf(DpSize.Zero)
    var floatingActionButtonSize by mutableStateOf(DpSize.Zero)

    var floatingActionButtonPosition by mutableStateOf(Alignment.BottomEnd)

    val snackbarPosition: Alignment
        get() = when (floatingActionButtonPosition) {
            Alignment.BottomEnd, Alignment.BottomStart -> {
                val remainingWidth = scaffoldListSize.width - snackbarSize.width - floatingActionButtonSize.width

                if (remainingWidth < 85.dp && floatingActionButtonPosition == Alignment.BottomStart) {
                    Alignment.BottomEnd
                } else if (remainingWidth < 85.dp && floatingActionButtonPosition == Alignment.BottomEnd) {
                    Alignment.BottomStart
                } else Alignment.BottomCenter
            }
            else -> Alignment.BottomCenter
        }

    val snackbarOverlapping: Boolean
        get() = when (floatingActionButtonPosition) {
            Alignment.BottomEnd, Alignment.BottomStart -> {
                val remainingWidth = scaffoldListSize.width - snackbarSize.width - floatingActionButtonSize.width

                remainingWidth < 15.dp
            }
            Alignment.BottomCenter -> true
            else -> false
        }
}

@Composable
fun rememberScaffoldListState(): ScaffoldListState {
    val popupHostState = remember { PopupHostState() }
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()

    return ScaffoldListState(popupHostState, snackbarHostState, lazyListState)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScaffoldList(
    scaffoldListState: ScaffoldListState,
    popupHost: @Composable (PopupHostState) -> Unit = { hostState -> PopupHost(hostState) },
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { hostState -> SnackbarHost(hostState) },
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: Alignment = Alignment.BottomEnd,
    content: @Composable BoxScope.(LazyListState) -> Unit
) {
    val density = LocalDensity.current

    scaffoldListState.floatingActionButtonPosition = floatingActionButtonPosition

    Column(modifier = modifier.onSizeChanged { size ->
        scaffoldListState.scaffoldListSize = with(density) {
            DpSize(size.width.toDp(), size.height.toDp())
        }
    }) {
        topBar()

        Box(modifier = Modifier.fillMaxSize()) {
            popupHost(scaffoldListState.popupHostState)

            content(scaffoldListState.lazyListState)

            Column(modifier = Modifier
                .align(alignment = floatingActionButtonPosition)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = if (scaffoldListState.snackbarOverlapping  &&
                        scaffoldListState.snackbarSize.height > 0.dp) {
                        scaffoldListState.snackbarSize.height
                    } else 16.dp
                )
                .onSizeChanged { size ->
                    scaffoldListState.floatingActionButtonSize = with(density) {
                        DpSize(size.width.toDp(), size.height.toDp())
                    }
                }
            ) {
                AnimatedContent(
                    targetState = scaffoldListState.snackbarOverlapping,
                    transitionSpec = {
                        if (targetState && !initialState) {
                            slideInVertically(
                                initialOffsetY = { 0 },
                                animationSpec = tween(easing = LinearEasing, durationMillis = 150)
                            ) with ExitTransition.None
                        } else slideInVertically(
                            initialOffsetY = { 0 },
                            animationSpec = tween(easing = LinearEasing, durationMillis = 75)
                        ) with ExitTransition.None
                    }
                ) {
                    val (scrollDirection, scrollPosition) = scaffoldListState.lazyListState.scrollInfo()

                    AnimatedVisibility(
                        visible = scaffoldListState.snackbarHostState.isSnackbarVisible() ||
                                scrollDirection == LazyListScrollDirection.BACKWARD ||
                                scrollPosition == LazyListScrollPosition.END,
                        enter = slideInVertically(animationSpec = tween(durationMillis = 250)) {
                            with(density) { 80.dp.roundToPx() }
                        },
                        exit = slideOutVertically(animationSpec = tween(durationMillis = 250)) {
                            with(density) { 80.dp.roundToPx() }
                        }
                    ) { floatingActionButton() }
                }
            }

            Column(modifier = Modifier.align(alignment = scaffoldListState.snackbarPosition).onSizeChanged { size ->
                scaffoldListState.snackbarSize = with(density) {
                    DpSize(size.width.toDp(), size.height.toDp())
                }
            }) { snackbarHost(scaffoldListState.snackbarHostState) }
        }
    }
}