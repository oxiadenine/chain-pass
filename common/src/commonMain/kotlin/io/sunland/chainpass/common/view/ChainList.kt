package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.NavigationState
import io.sunland.chainpass.common.Screen
import io.sunland.chainpass.common.Settings
import kotlinx.coroutines.launch

enum class ChainListAction { NONE, SELECT, REMOVE, STORE, UNSTORE }

@Composable
fun ChainList(
    viewModel: ChainListViewModel,
    settingsState: MutableState<Settings>,
    navigationState: NavigationState,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

    val chainListActionState = remember { mutableStateOf(ChainListAction.SELECT) }

    val isWorkInProgressState = remember { mutableStateOf(false) }
    val isInputDialogVisibleState = remember { mutableStateOf(false) }

    if (isWorkInProgressState.value) {
        LoadingIndicator()
    }

    if (isInputDialogVisibleState.value) {
        when (chainListActionState.value) {
            ChainListAction.SELECT -> ChainListItemKeyInput(
                onKey = { chainKey ->
                    snackbarHostState.currentSnackbarData?.dismiss()

                    isInputDialogVisibleState.value = false

                    val chain = viewModel.chainSelected!!.apply { key = chainKey }

                    viewModel.setSelected()

                    coroutineScope.launch {
                        viewModel.select(chain).onSuccess {
                            navigationState.chainState.value = chain
                            navigationState.screenState.value = Screen.CHAIN_LINK_LIST
                        }.onFailure { exception ->
                            snackbarHostState.showSnackbar(exception.message ?: "Error")
                        }
                    }
                },
                onCancel = { isInputDialogVisibleState.value = false }
            )
            ChainListAction.REMOVE -> ChainListItemKeyInput(
                onKey = { chainKey ->
                    snackbarHostState.currentSnackbarData?.dismiss()

                    isInputDialogVisibleState.value = false

                    val chain = viewModel.chainSelected!!.apply { key = chainKey }

                    viewModel.removeLater(chain)

                    coroutineScope.launch {
                        when (snackbarHostState.showSnackbar(
                            message = "${chain.name.value} removed",
                            actionLabel = "Dismiss",
                            duration = SnackbarDuration.Short
                        )) {
                            SnackbarResult.ActionPerformed -> viewModel.undoRemove(chain)
                            SnackbarResult.Dismissed -> viewModel.remove(chain).onFailure { exception ->
                                viewModel.undoRemove(chain)

                                snackbarHostState.showSnackbar(exception.message ?: "Error")
                            }
                        }
                    }
                },
                onCancel = { isInputDialogVisibleState.value = false }
            )
            ChainListAction.STORE -> ChainListStoreInput(
                isSingle = false,
                onStore = { storeOptions ->
                    snackbarHostState.currentSnackbarData?.dismiss()

                    isInputDialogVisibleState.value = false

                    coroutineScope.launch {
                        isWorkInProgressState.value = true

                        viewModel.store(storeOptions).onSuccess { fileName ->
                            isWorkInProgressState.value = false

                            snackbarHostState.showSnackbar("Stored to $fileName")
                        }.onFailure { exception ->
                            isWorkInProgressState.value = false

                            snackbarHostState.showSnackbar(exception.message ?: "Error")
                        }
                    }
                },
                onCancel = { isInputDialogVisibleState.value = false }
            )
            ChainListAction.UNSTORE -> ChainListUnstoreInput(
                isSingle = false,
                onUnstore = { filePath ->
                    snackbarHostState.currentSnackbarData?.dismiss()

                    isInputDialogVisibleState.value = false

                    coroutineScope.launch {
                        isWorkInProgressState.value = true

                        viewModel.unstore(filePath).onSuccess {
                            viewModel.getAll()

                            isWorkInProgressState.value = false

                            snackbarHostState.showSnackbar("Unstored from ${filePath.fileName}")
                        }.onFailure { exception ->
                            isWorkInProgressState.value = false

                            snackbarHostState.showSnackbar(exception.message ?: "Error")
                        }
                    }
                },
                onCancel = { isInputDialogVisibleState.value = false }
            )
            ChainListAction.NONE -> Unit
        }
    }

    LaunchedEffect(Unit) { viewModel.getAll() }

    Column(modifier = Modifier.fillMaxSize()) {
        ChainListTopBar(
            onSettings = { navigationState.screenState.value = Screen.SETTINGS },
            onSync = {
                snackbarHostState.currentSnackbarData?.performAction()

                coroutineScope.launch {
                    if (settingsState.value.deviceAddress.isEmpty()) {
                        snackbarHostState.showSnackbar("You have to set Device Address on Settings")
                    } else {
                        isWorkInProgressState.value = true

                        viewModel.sync(settingsState.value.deviceAddress).onSuccess {
                            viewModel.getAll()

                            isWorkInProgressState.value = false
                        }.onFailure { exception ->
                            isWorkInProgressState.value = false

                            snackbarHostState.showSnackbar(exception.message ?: "Error")
                        }
                    }
                }
            },
            onAdd = { viewModel.draft() },
            onStore = {
                chainListActionState.value = ChainListAction.STORE
                isInputDialogVisibleState.value = true
            },
            onUnstore = {
                chainListActionState.value = ChainListAction.UNSTORE
                isInputDialogVisibleState.value = true
            }
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
                                        viewModel.setSelected(chain)

                                        chainListActionState.value = ChainListAction.SELECT
                                        isInputDialogVisibleState.value = true
                                    },
                                    onRemove = {
                                        viewModel.setSelected(chain)

                                        chainListActionState.value = ChainListAction.REMOVE
                                        isInputDialogVisibleState.value = true
                                    }
                                )
                            }
                            Chain.Status.DRAFT -> key(chain.id) {
                                ChainListItemDraft(
                                    chain = chain,
                                    onNew = { viewModel.new(chain) },
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