package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.ChainLink

class ChainLinkSearchListState(val chain: Chain, private val chainLinks: List<ChainLink>) {
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
fun rememberChainLinkSearchListState(chain: Chain, chainLinks: List<ChainLink>) = remember {
    ChainLinkSearchListState(chain, chainLinks)
}

@Composable
fun ChainLinkSearchList(
    state: ChainLinkSearchListState,
    onTopAppBarBackClick: (Chain) -> Unit,
    onListItemClick: (ChainLink) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ChainLinkSearchListTopAppBar(
            onBackClick = { onTopAppBarBackClick(state.chain) },
            onKeywordChange = { keyword -> state.search(keyword) }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            val chainLinks = state.chainLinkSearchListState.toTypedArray()

            if (chainLinks.isEmpty()) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "No Chain Links")
                    Icon(imageVector = Icons.Default.List, contentDescription = null)
                }
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