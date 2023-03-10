package io.sunland.chainpass.common.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*

enum class LazyListScrollDirection { FORWARD, BACKWARD }

enum class LazyListScrollPosition { START, END, BETWEEN }

data class LazyListScrollInfo(val direction: LazyListScrollDirection, val position: LazyListScrollPosition)

@Composable
fun LazyListState.scrollInfo(): LazyListScrollInfo {
    val prevFirstVisibleItemIndexState = remember { mutableStateOf(firstVisibleItemIndex) }
    val prevFirstVisibleItemScrollOffsetState = remember { mutableStateOf(firstVisibleItemScrollOffset) }
    val lastVisibleItemIndexState = remember { mutableStateOf(layoutInfo.visibleItemsInfo.lastIndex) }

    return remember {
        derivedStateOf {
            if (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0) {
                LazyListScrollInfo(LazyListScrollDirection.BACKWARD, LazyListScrollPosition.START)
            } else if (firstVisibleItemIndex == prevFirstVisibleItemIndexState.value) {
                if (firstVisibleItemIndex + lastVisibleItemIndexState.value == layoutInfo.totalItemsCount - 1 ||
                    firstVisibleItemIndex + lastVisibleItemIndexState.value == layoutInfo.totalItemsCount) {
                    LazyListScrollInfo(LazyListScrollDirection.FORWARD, LazyListScrollPosition.END)
                } else LazyListScrollInfo(LazyListScrollDirection.FORWARD, LazyListScrollPosition.BETWEEN)
            } else {
                if (firstVisibleItemIndex < prevFirstVisibleItemIndexState.value) {
                    if (firstVisibleItemScrollOffset == 0) {
                        LazyListScrollInfo(LazyListScrollDirection.BACKWARD, LazyListScrollPosition.START)
                    } else LazyListScrollInfo(LazyListScrollDirection.BACKWARD, LazyListScrollPosition.BETWEEN)
                } else {
                    if (firstVisibleItemIndex + lastVisibleItemIndexState.value == layoutInfo.totalItemsCount - 1 ||
                        firstVisibleItemIndex + lastVisibleItemIndexState.value == layoutInfo.totalItemsCount) {
                        LazyListScrollInfo(LazyListScrollDirection.FORWARD, LazyListScrollPosition.END)
                    } else LazyListScrollInfo(LazyListScrollDirection.FORWARD, LazyListScrollPosition.BETWEEN)
                }
            }.also {
                prevFirstVisibleItemIndexState.value = firstVisibleItemIndex
                prevFirstVisibleItemScrollOffsetState.value = firstVisibleItemScrollOffset
                lastVisibleItemIndexState.value = layoutInfo.visibleItemsInfo.lastIndex
            }
        }
    }.value
}