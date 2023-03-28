package io.sunland.chainpass.common.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import io.sunland.chainpass.common.Platform
import io.sunland.chainpass.common.platform
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PopupHostState {
    private val mutex = Mutex()

    var currentPopupData by mutableStateOf<PopupData?>(null)
        private set

    suspend fun showPopup(message: String, duration: Long = 2000L) = mutex.withLock {
        try {
            suspendCancellableCoroutine { continuation ->
                currentPopupData = PopupData(message, duration, continuation)
            }
        } finally {
            currentPopupData = null

        }
    }

    class PopupData(
        val message: String,
        val duration: Long,
        private val continuation: CancellableContinuation<Unit>
    ) {
        fun dismiss() {
            if (continuation.isActive) continuation.cancel()
        }
    }
}

@Composable
fun PopupHost(
    hostState: PopupHostState,
    popupContent: @Composable (PopupHostState.PopupData) -> Unit = {}
) {
    val currentPopupData = hostState.currentPopupData

    LaunchedEffect(currentPopupData) {
        if (currentPopupData != null) {
            delay(currentPopupData.duration)

            currentPopupData.dismiss()
        }
    }

    AnimatedPopup(popupData = hostState.currentPopupData, content = popupContent)
}

@Composable
private fun AnimatedPopup(
    popupData: PopupHostState.PopupData?,
    alignment: Alignment = Alignment.TopCenter,
    offset: DpOffset = DpOffset(x = 0.dp, y = 16.dp),
    content: @Composable (PopupHostState.PopupData) -> Unit
) {
    val popupState = remember { AnimatedPopupState<PopupHostState.PopupData?>() }

    if (popupData != popupState.popupData) {
        popupState.popupData = popupData

        val popupItemsKeys = popupState.items.map { it.key }.toMutableList()

        if (!popupItemsKeys.contains(popupData)) {
            popupItemsKeys.add(popupData)
        }

        popupState.items.clear()

        popupItemsKeys.filterNotNull().mapTo(popupState.items) { popupItemKey ->
            AnimatedPopupItem(popupItemKey) { children ->
                val isVisible = popupItemKey == popupData
                val duration = if (isVisible) 250 else 250
                val delay = 250
                val animationDelay = if (isVisible && popupItemsKeys.filterNotNull().size != 1) delay else 0

                val opacity = animatedOpacity(
                    animation = tween(
                        easing = LinearEasing,
                        delayMillis = animationDelay,
                        durationMillis = duration
                    ),
                    visible = isVisible,
                    onAnimationFinish = {
                        if (popupItemKey != popupState.popupData) {
                            popupState.items.removeAll { it.key == popupItemKey }
                            popupState.scope?.invalidate()
                        }
                    }
                )
                val scale = animatedScale(
                    animation = tween(
                        easing = FastOutSlowInEasing,
                        delayMillis = animationDelay,
                        durationMillis = duration
                    ),
                    visible = isVisible
                )

                Box(modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value,
                        alpha = opacity.value
                    )
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        dismiss { popupItemKey.dismiss(); true }
                    }
                ) { children() }
            }
        }
    }

    Popup(
        alignment = alignment,
        offset = with(LocalDensity.current) {
            IntOffset(x = offset.x.roundToPx(), y = offset.y.roundToPx())
        }
    ) {
        popupState.scope = currentRecomposeScope
        popupState.items.forEach { (item, opacity) ->
            key(item) {
                opacity {
                    Surface(
                        modifier = Modifier.padding(all = 16.dp),
                        shape = if (platform == Platform.DESKTOP) RectangleShape else MaterialTheme.shapes.extraSmall,
                        tonalElevation = 4.dp
                    ) { content(item!!) }
                }
            }
        }
    }
}

private class AnimatedPopupState<T> {
    var popupData: Any? = Any()
    var items = mutableListOf<AnimatedPopupItem<T>>()
    var scope: RecomposeScope? = null
}

private data class AnimatedPopupItem<T>(
    val key: T,
    val transition: @Composable (content: @Composable () -> Unit) -> Unit
)

@Composable
private fun animatedOpacity(
    animation: AnimationSpec<Float>,
    visible: Boolean,
    onAnimationFinish: () -> Unit = {}
): State<Float> {
    val alpha = remember { Animatable(if (!visible) 1f else 0f) }

    LaunchedEffect(visible) {
        alpha.animateTo(
            if (visible) 1f else 0f,
            animationSpec = animation
        )
        onAnimationFinish()
    }

    return alpha.asState()
}

@Composable
private fun animatedScale(animation: AnimationSpec<Float>, visible: Boolean): State<Float> {
    val scale = remember { Animatable(if (!visible) 1f else 0.8f) }

    LaunchedEffect(visible) {
        scale.animateTo(
            if (visible) 1f else 0.8f,
            animationSpec = animation
        )
    }

    return scale.asState()
}