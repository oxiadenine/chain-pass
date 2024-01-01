package io.sunland.chainpass.common.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*

enum class LazyListScrollDirection { FORWARD, BACKWARD }

@Composable
fun LazyListState.scrollDirection(): LazyListScrollDirection {
    val prevFirstVisibleItemIndexState = remember { mutableStateOf(firstVisibleItemIndex) }
    val prevFirstVisibleItemScrollOffsetState = remember { mutableStateOf(firstVisibleItemScrollOffset) }

    return remember {
        derivedStateOf {
            if (prevFirstVisibleItemIndexState.value != firstVisibleItemIndex) {
                if (prevFirstVisibleItemIndexState.value > firstVisibleItemIndex) {
                    LazyListScrollDirection.BACKWARD
                } else LazyListScrollDirection.FORWARD
            } else {
                if (prevFirstVisibleItemScrollOffsetState.value >= firstVisibleItemScrollOffset) {
                    LazyListScrollDirection.BACKWARD
                } else LazyListScrollDirection.FORWARD
            }.also {
                prevFirstVisibleItemIndexState.value = firstVisibleItemIndex
                prevFirstVisibleItemScrollOffsetState.value = firstVisibleItemScrollOffset
            }
        }
    }.value
}