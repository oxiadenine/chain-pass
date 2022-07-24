package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.component.VerticalScrollbar

@Composable
fun ChainLinkList(
    viewModel: ChainLinkListViewModel,
    onNew: (ChainLink) -> Unit,
    onEdit: (ChainLink) -> Unit,
    onRemove: (ChainLink) -> Unit,
    onPasswordCopy: (ChainLink) -> Unit,
    onSync: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (viewModel.isSearchState.value) {
            ChainLinkSearchListTopBar(
                onBack = { viewModel.endSearch() },
                onSearch = { keyword -> viewModel.search(keyword) }
            )
        } else {
            ChainLinkListTopBar(
                onBack = {
                    viewModel.cancelEdit()

                    onBack()
                },
                onSync = onSync,
                onAdd = { viewModel.draft() },
                onSearch = { viewModel.startSearch() }
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
                            ChainLinkSearchListItem(
                                chainLink = chainLink,
                                onPasswordLock = { isPasswordLocked ->
                                    if (isPasswordLocked) {
                                        viewModel.unlockPassword(chainLink)
                                    } else viewModel.lockPassword(chainLink)
                                },
                                onPasswordCopy = { onPasswordCopy(chainLink) }
                            )
                        } else {
                            when (chainLink.status) {
                                ChainLink.Status.ACTUAL -> ChainLinkListItem(
                                    chainLink = chainLink,
                                    onEdit = {
                                        viewModel.cancelEdit()
                                        viewModel.startEdit(chainLink)
                                    },
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
                                        onCancel = { viewModel.cancelEdit() }
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