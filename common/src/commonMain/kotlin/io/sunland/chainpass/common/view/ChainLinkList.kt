package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.ChainLink
import kotlinx.coroutines.launch

@Composable
fun ChainLinkList(
    viewModel: ChainLinkListViewModel,
    onNew: (ChainLink) -> Unit,
    onEdit: (ChainLink) -> Unit,
    onRemove: (ChainLink) -> Unit,
    onSearch: () -> Unit,
    onPasswordCopy: (ChainLink) -> Unit,
    onSync: () -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (viewModel.isSearchState.value) {
            ChainLinkSearchListTopBar(
                keywordState = viewModel.searchKeywordState,
                onBack = { viewModel.endSearch() },
                onSearch = { keyword -> viewModel.search(keyword) }
            )
        } else {
            ChainLinkListTopBar(
                onBack = {
                    viewModel.cancelEdits()

                    onBack()
                },
                onSync = onSync,
                onAdd = {
                    viewModel.draft()

                    coroutineScope.launch {
                        lazyListState.scrollToItem(viewModel.chainLinkListState.size - 1)
                    }
                },
                onSearch = {
                    viewModel.rejectDrafts()
                    viewModel.cancelEdits()
                    viewModel.startSearch()

                    onSearch()
                }
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            val chainLinks = if (viewModel.isSearchState.value) {
                viewModel.chainLinkSearchListState.toTypedArray()
            } else viewModel.chainLinkListState.toTypedArray()

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
                LazyColumn(modifier = Modifier.fillMaxSize(), state = lazyListState) {
                    items(chainLinks, key = { chainLink -> chainLink.id }) { chainLink ->
                        if (viewModel.isSearchState.value) {
                            ChainLinkSearchListItem(
                                chainLink = chainLink,
                                onSelect = {
                                    chainLink.status = ChainLink.Status.SELECT

                                    viewModel.endSearch()
                                }
                            )
                        } else {
                            when (chainLink.status) {
                                ChainLink.Status.ACTUAL, ChainLink.Status.SELECT -> ChainLinkListItem(
                                    chainLink = chainLink,
                                    onEdit = { viewModel.startEdit(chainLink) },
                                    onDelete = {
                                        viewModel.removeLater(chainLink)

                                        onRemove(chainLink)
                                    },
                                    onPasswordLock = { isPasswordLocked ->
                                        if (isPasswordLocked) {
                                            viewModel.unlockPassword(chainLink)
                                        } else viewModel.lockPassword(chainLink)
                                    },
                                    onPasswordCopy = { onPasswordCopy(chainLink) }
                                )
                                ChainLink.Status.DRAFT -> ChainLinkListItemDraft(
                                    chainLink = chainLink,
                                    onNew = { onNew(chainLink) },
                                    onCancel = { viewModel.rejectDraft(chainLink) }
                                )
                                ChainLink.Status.EDIT -> key(chainLink.id) {
                                    ChainLinkListItemEdit(
                                        chainLink = chainLink,
                                        onEdit = { onEdit(chainLink) },
                                        onCancel = { viewModel.cancelEdit(chainLink) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}