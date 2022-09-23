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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.StorageOptions

@Composable
fun ChainLinkList(
    storageOptions: StorageOptions,
    viewModel: ChainLinkListViewModel,
    onBack: () -> Unit,
    onSync: () -> Unit,
    onNew: (ChainLink) -> Unit,
    onEdit: (ChainLink) -> Unit,
    onRemove: (ChainLink) -> Unit,
    onSearch: () -> Unit,
    onStore: (StorageOptions) -> Unit,
    onUnstore: (StorageOptions, FilePath) -> Unit
) {
    val storeDialogVisibleState = remember { mutableStateOf(false) }
    val unstoreDialogVisibleState = remember { mutableStateOf(false) }

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
                onAdd = { viewModel.draft() },
                onSearch = {
                    viewModel.rejectDrafts()
                    viewModel.cancelEdits()
                    viewModel.startSearch()

                    onSearch()
                },
                onStore = { storeDialogVisibleState.value = true },
                onUnstore = { unstoreDialogVisibleState.value = true }
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
                val lazyListState = rememberLazyListState()

                LazyColumn(modifier = Modifier.fillMaxSize(), state = lazyListState) {
                    items(chainLinks, key = { chainLink -> chainLink.id }) { chainLink ->
                        if (viewModel.isSearchState.value) {
                            ChainLinkSearchListItem(
                                chainLink = chainLink,
                                onSelect = { viewModel.endSearch(chainLink) }
                            )
                        } else {
                            when (chainLink.status) {
                                ChainLink.Status.ACTUAL -> {
                                    val clipboardManager = LocalClipboardManager.current

                                    ChainLinkListItem(
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
                                        onPasswordCopy = {
                                            clipboardManager.setText(AnnotatedString(chainLink.password.value))
                                        }
                                    )
                                }
                                ChainLink.Status.DRAFT -> key(chainLink.id) {
                                    ChainLinkListItemDraft(
                                        chainLink = chainLink,
                                        onNew = { onNew(chainLink) },
                                        onCancel = { viewModel.rejectDraft(chainLink) }
                                    )
                                }
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

                LaunchedEffect(
                    viewModel.isSearchState.value,
                    viewModel.searchKeywordState.value,
                    viewModel.chainLinkSearchListState.size
                ) { lazyListState.scrollToItem(0) }

                viewModel.chainLinkLatestIndex.takeIf { index -> index != -1 }?.let { index ->
                    LaunchedEffect(index) {
                        lazyListState.scrollToItem(index)
                    }
                }
            }

            if (storeDialogVisibleState.value) {
                ChainLinkListStoreOptions(
                    storageOptions = storageOptions,
                    onDismiss = { storeDialogVisibleState.value = false },
                    onConfirm = { storageOptions ->
                        storeDialogVisibleState.value = false

                        onStore(storageOptions)
                    }
                )
            }

            if (unstoreDialogVisibleState.value) {
                ChainLinkListUnstoreInput(
                    onDismiss = { unstoreDialogVisibleState.value = false },
                    onConfirm = { filePath ->
                        unstoreDialogVisibleState.value = false

                        onUnstore(storageOptions, filePath)
                    }
                )
            }
        }
    }
}