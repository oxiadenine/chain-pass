package io.sunland.chainpass.common.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*

enum class LazyListScrollDirection { FORWARD, BACKWARD }

enum class LazyListScrollPosition { START, END, BETWEEN }

data class LazyListScrollInfo(val direction: LazyListScrollDirection, val position: LazyListScrollPosition)

@Composable
fun LazyListState.scrollInfo(): LazyListScrollInfo {
    var prevFirstVisibleItemIndex by remember { mutableStateOf(firstVisibleItemIndex) }
    var prevFirstVisibleItemScrollOffset by remember { mutableStateOf(firstVisibleItemScrollOffset) }
    var lastVisibleItemIndex by remember { mutableStateOf(layoutInfo.visibleItemsInfo.lastIndex) }

    return remember {
        derivedStateOf {
            if (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0) {
                LazyListScrollInfo(LazyListScrollDirection.BACKWARD, LazyListScrollPosition.START)
            } else if (firstVisibleItemIndex == prevFirstVisibleItemIndex) {
                if (firstVisibleItemIndex + lastVisibleItemIndex == layoutInfo.totalItemsCount - 1 ||
                    firstVisibleItemIndex + lastVisibleItemIndex == layoutInfo.totalItemsCount) {
                    LazyListScrollInfo(LazyListScrollDirection.FORWARD, LazyListScrollPosition.END)
                } else {
                    if (firstVisibleItemScrollOffset <= prevFirstVisibleItemScrollOffset) {
                        LazyListScrollInfo(LazyListScrollDirection.BACKWARD, LazyListScrollPosition.BETWEEN)
                    } else LazyListScrollInfo(LazyListScrollDirection.FORWARD, LazyListScrollPosition.BETWEEN)
                }
            } else {
                if (firstVisibleItemIndex < prevFirstVisibleItemIndex) {
                    if (firstVisibleItemScrollOffset == 0) {
                        LazyListScrollInfo(LazyListScrollDirection.BACKWARD, LazyListScrollPosition.START)
                    } else LazyListScrollInfo(LazyListScrollDirection.BACKWARD, LazyListScrollPosition.BETWEEN)
                } else {
                    if (firstVisibleItemIndex + lastVisibleItemIndex == layoutInfo.totalItemsCount - 1 ||
                        firstVisibleItemIndex + lastVisibleItemIndex == layoutInfo.totalItemsCount) {
                        LazyListScrollInfo(LazyListScrollDirection.FORWARD, LazyListScrollPosition.END)
                    } else LazyListScrollInfo(LazyListScrollDirection.FORWARD, LazyListScrollPosition.BETWEEN)
                }
            }.also {
                prevFirstVisibleItemIndex = firstVisibleItemIndex
                prevFirstVisibleItemScrollOffset = firstVisibleItemScrollOffset
                lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastIndex
            }
        }
    }.value
}