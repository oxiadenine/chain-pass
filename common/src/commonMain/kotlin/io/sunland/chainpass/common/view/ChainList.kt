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
import io.sunland.chainpass.common.StorageOptions

enum class InputActionType { SELECT, REMOVE, STORE, UNSTORE }

@Composable
fun ChainList(
    serverAddress: ServerAddress,
    viewModel: ChainListViewModel,
    onSync: () -> Unit,
    onNew: (Chain) -> Unit,
    onSelect: (Chain) -> Unit,
    onRemove: (Chain) -> Unit,
    onStore: (Chain, StorageOptions) -> Unit,
    onUnstore: (Chain.Key, FilePath) -> Unit,
    onDisconnect: () -> Unit
) {
    val inputActionTypeState = remember { mutableStateOf(InputActionType.SELECT) }
    val inputActionDialogVisibleState = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ChainListTopBar(
            serverAddress = serverAddress,
            onSync = onSync,
            onAdd = { viewModel.draft() },
            onUnstore = {
                inputActionTypeState.value = InputActionType.UNSTORE
                inputActionDialogVisibleState.value = true
            },
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
                                ChainListItem(
                                    chain = chain,
                                    onSelect = {
                                        viewModel.chainState.value = chain

                                        inputActionTypeState.value = InputActionType.SELECT
                                        inputActionDialogVisibleState.value = true
                                    },
                                    onRemove = {
                                        viewModel.chainState.value = chain

                                        inputActionTypeState.value = InputActionType.REMOVE
                                        inputActionDialogVisibleState.value = true
                                    },
                                    onStore = {
                                        viewModel.chainState.value = chain

                                        inputActionTypeState.value = InputActionType.STORE
                                        inputActionDialogVisibleState.value = true
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

            if (inputActionDialogVisibleState.value) {
                ChainListItemKeyInput(
                    inputActionType = inputActionTypeState.value,
                    onDismiss = { inputActionDialogVisibleState.value = false },
                    onConfirm = { chainKey, storageOptions, filePath ->
                        val chain = viewModel.chainState.value?.let { chain ->
                            Chain(chain).apply { key = chainKey }
                        }

                        when (inputActionTypeState.value) {
                            InputActionType.SELECT -> onSelect(chain!!)
                            InputActionType.REMOVE -> {
                                viewModel.removeLater(viewModel.chainState.value!!)

                                onRemove(chain!!)
                            }
                            InputActionType.STORE -> onStore(chain!!, storageOptions!!)
                            InputActionType.UNSTORE -> onUnstore(chainKey, filePath!!)
                        }

                        viewModel.chainState.value = null

                        inputActionDialogVisibleState.value = false
                    }
                )
            }
        }
    }
}