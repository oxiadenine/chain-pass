package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.component.VerticalScrollbar

@Composable
fun ChainLinkList(
    viewModel: ChainLinkListViewModel,
    onItemNew: (ChainLink) -> Unit,
    onItemEdit: (ChainLink) -> Unit,
    onItemRemove: (ChainLink) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (viewModel.isSearchState.value) {
            ChainLinkSearchListTopBar(
                onIconArrowBackClick = { viewModel.endSearch() },
                onSearch = { keyword -> viewModel.search(keyword) }
            )
        } else {
            ChainLinkListTopBar(
                onIconArrowBackClick = onBack,
                onIconRefreshClick = onRefresh,
                onIconAddClick = { viewModel.draft() },
                onIconSearchClick = { viewModel.startSearch() }
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            val chainLinks = if (viewModel.isSearchState.value) {
                viewModel.chainLinkSearchListState.toList()
            } else viewModel.chainLinkListState.toList()

            if (chainLinks.isEmpty()) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    if (viewModel.isSearchState.value) {
                        Text(text = "No Chain Links")
                        Icon(imageVector = Icons.Default.List, contentDescription = null)
                    } else {
                        Text(text = "New Chain Link")
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                }
            } else {
                val scrollState = rememberScrollState()

                Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                    chainLinks.forEach { chainLink ->
                        if (viewModel.isSearchState.value) {
                            ChainLinkSearchListItem(chainLink = chainLink)
                        } else {
                            when (chainLink.status) {
                                ChainLink.Status.ACTUAL -> ChainLinkListItem(
                                    chainLink = chainLink,
                                    onIconEditClick = { viewModel.startEdit(chainLink.id) },
                                    onIconDeleteClick = {
                                        viewModel.removeLater(chainLink)

                                        onItemRemove(chainLink)
                                    }
                                )
                                ChainLink.Status.DRAFT -> ChainLinkListItemDraft(
                                    chainLink = chainLink,
                                    onIconDoneClick = { onItemNew(chainLink) },
                                    onIconClearClick = { viewModel.rejectDraft(chainLink) }
                                )
                                ChainLink.Status.EDIT -> key(chainLink.id) {
                                    ChainLinkListItemEdit(
                                        chainLink = chainLink,
                                        onIconDoneClick = { onItemEdit(chainLink) },
                                        onIconClearClick = { viewModel.endEdit(chainLink) }
                                    )
                                }
                            }
                        }
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd),
                    scrollState = scrollState
                )
            }
        }
    }
}
