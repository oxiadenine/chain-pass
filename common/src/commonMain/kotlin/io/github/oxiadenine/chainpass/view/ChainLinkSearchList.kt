package io.github.oxiadenine.chainpass.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.oxiadenine.chainpass.ChainLink
import io.github.oxiadenine.chainpass.component.NavigationState
import io.github.oxiadenine.common.generated.resources.Res
import io.github.oxiadenine.common.generated.resources.list_chainLink_search_empty_text
import org.jetbrains.compose.resources.stringResource

class ChainLinkSearchListRouteArgument(val chainLinks: List<ChainLink>) : NavigationState.RouteArgument()

class ChainLinkSearchListState(private val chainLinks: List<ChainLink>) {
    val chainLinkSearchListState = chainLinks.toMutableStateList()

    fun search(keyword: String) {
        val chainLinks = chainLinks
            .filter { chainLink -> chainLink.name.value.lowercase().contains(keyword.lowercase()) }
            .sortedBy { chainLink -> chainLink.name.value }

        chainLinkSearchListState.clear()
        chainLinkSearchListState.addAll(chainLinks)
    }
}

@Composable
fun rememberChainLinkSearchListState(chainLinks: List<ChainLink>) = remember {
    ChainLinkSearchListState(chainLinks)
}

@Composable
fun ChainLinkSearchList(
    state: ChainLinkSearchListState,
    onTopAppBarBackClick: () -> Unit,
    onListItemClick: (ChainLink) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ChainLinkSearchListTopAppBar(
            onBackClick = { onTopAppBarBackClick() },
            onKeywordChange = { keyword -> state.search(keyword) }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            val chainLinks = state.chainLinkSearchListState.toTypedArray()

            if (chainLinks.isEmpty()) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) { Text(text = stringResource(Res.string.list_chainLink_search_empty_text)) }
            } else {
                val lazyListState = rememberLazyListState()

                LazyColumn(state = lazyListState, modifier = Modifier.fillMaxWidth()) {
                    items(items = chainLinks, key = { chainLink -> chainLink.id }) { chainLink ->
                        ChainLinkSearchListItem(
                            onClick = { onListItemClick(chainLink) },
                            name = chainLink.name.value,
                            description = chainLink.description.value
                        )
                    }
                }
            }
        }
    }
}