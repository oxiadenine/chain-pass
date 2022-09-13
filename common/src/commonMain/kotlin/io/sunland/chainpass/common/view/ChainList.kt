package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Chain

@Composable
fun ChainList(
    serverAddress: ServerAddress,
    viewModel: ChainListViewModel,
    onSync: () -> Unit,
    onNew: (Chain) -> Unit,
    onSelect: (Chain) -> Unit,
    onRemove: (Chain) -> Unit,
    onDisconnect: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ChainListTopBar(
            serverAddress = serverAddress,
            onSync = onSync,
            onAdd = { viewModel.draft() },
            onDisconnect = onDisconnect
        )
        Box(modifier = Modifier.fillMaxSize()) {
            if (viewModel.chainListState.isEmpty()) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    Text(text = "New Chain")
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            } else {
                val lazyListState = rememberLazyListState()

                LazyColumn(modifier = Modifier.fillMaxSize(), state = lazyListState) {
                    items(viewModel.chainListState.toTypedArray(), key = { chain -> chain.id }) { chain ->
                        when (chain.status) {
                            Chain.Status.ACTUAL -> {
                                val keyInputDialogVisible = remember { mutableStateOf(false) }

                                if (keyInputDialogVisible.value) {
                                    ChainListItemKeyInput(
                                        onDismiss = {
                                            viewModel.chainSelectState.value = null
                                            viewModel.chainRemoveState.value = null

                                            keyInputDialogVisible.value = false
                                        },
                                        onConfirm = { chainKey ->
                                            viewModel.select(chainKey)?.let(onSelect)
                                            viewModel.removeLater(chainKey)?.let(onRemove)

                                            viewModel.chainSelectState.value = null
                                            viewModel.chainRemoveState.value = null

                                            keyInputDialogVisible.value = false
                                        }
                                    )
                                }

                                ChainListItem(
                                    chain = chain,
                                    onSelect = {
                                        viewModel.chainSelectState.value = chain

                                        keyInputDialogVisible.value = true
                                    },
                                    onRemove = {
                                        viewModel.chainRemoveState.value = chain

                                        keyInputDialogVisible.value = true
                                    }
                                )
                            }
                            Chain.Status.DRAFT -> key(chain.id) {
                                ChainListItemDraft(
                                    chain = chain,
                                    onNew = { onNew(chain) },
                                    onCancel = { viewModel.rejectDraft(chain) }
                                )
                            }
                        }
                    }
                }

                viewModel.chainLatestIndex.takeIf { index -> index != -1 }?.let { index ->
                    LaunchedEffect(index) { lazyListState.scrollToItem(index) }
                }
            }
        }
    }
}