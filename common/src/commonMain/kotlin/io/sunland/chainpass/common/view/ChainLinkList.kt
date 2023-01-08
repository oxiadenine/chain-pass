package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.component.PopupText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ChainLinkListAction { NONE, STORE, UNSTORE }

@Composable
fun ChainLinkList(
    viewModel: ChainLinkListViewModel,
    settingsState: MutableState<Settings>,
    navigationState: NavigationState,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

    val chainLinkListActionState = remember { mutableStateOf(ChainLinkListAction.NONE) }

    val isWorkInProgressState = remember { mutableStateOf(false) }
    val isInputDialogVisibleState = remember { mutableStateOf(false) }

    val popupMessageState = remember { mutableStateOf("") }
    val isPopupVisibleState = remember { mutableStateOf(false) }

    if (isWorkInProgressState.value) {
        LoadingIndicator()
    }

    if (isInputDialogVisibleState.value) {
        when (chainLinkListActionState.value) {
            ChainLinkListAction.STORE -> ChainListStoreInput(
                isSingle = true,
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
            ChainLinkListAction.UNSTORE -> ChainListUnstoreInput(
                isSingle = true,
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
            ChainLinkListAction.NONE -> Unit
        }
    }

    if (isPopupVisibleState.value) {
        PopupText(
            alignment = Alignment.BottomCenter,
            offset = IntOffset(x = 0, y = -80),
            text = popupMessageState.value
        )
    }

    LaunchedEffect(Unit) {
        viewModel.chain = navigationState.chainState.value
        viewModel.getAll()
    }

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
                    snackbarHostState.currentSnackbarData?.dismiss()

                    viewModel.back()

                    navigationState.screenState.value = Screen.CHAIN_LIST
                },
                onSync = {
                    snackbarHostState.currentSnackbarData?.performAction()

                    coroutineScope.launch {
                        if (settingsState.value.deviceAddress.isEmpty()) {
                            snackbarHostState.showSnackbar("You have to set sync options on Settings")
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
                onSearch = {
                    snackbarHostState.currentSnackbarData?.dismiss()

                    viewModel.rejectDrafts()
                    viewModel.cancelEdits()
                    viewModel.startSearch()
                },
                onStore = {
                    chainLinkListActionState.value = ChainLinkListAction.STORE
                    isInputDialogVisibleState.value = true
                },
                onUnstore = {
                    chainLinkListActionState.value = ChainLinkListAction.UNSTORE
                    isInputDialogVisibleState.value = true
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
                val lazyListState = rememberLazyListState()

                LazyColumn(modifier = Modifier.fillMaxSize(), state = lazyListState) {
                    items(chainLinks, key = { chainLink -> chainLink.id }) { chainLink ->
                        if (viewModel.isSearchState.value) {
                            ChainLinkSearchListItem(
                                chainLink = chainLink,
                                onSelect = { viewModel.endSearch(chainLink) }
                            )
                        } else {
                            val clipboardManager = LocalClipboardManager.current

                            when (chainLink.status) {
                                ChainLink.Status.ACTUAL -> {
                                    ChainLinkListItem(
                                        chainLink = chainLink,
                                        onEdit = { viewModel.startEdit(chainLink) },
                                        onDelete = {
                                            snackbarHostState.currentSnackbarData?.dismiss()

                                            viewModel.removeLater(chainLink)

                                            coroutineScope.launch {
                                                when (snackbarHostState.showSnackbar(
                                                    message = "${chainLink.name.value} removed",
                                                    actionLabel = "Dismiss",
                                                    duration = SnackbarDuration.Short
                                                )) {
                                                    SnackbarResult.ActionPerformed -> viewModel.undoRemove(chainLink)
                                                    SnackbarResult.Dismissed -> viewModel.remove(chainLink)
                                                }
                                            }
                                        },
                                        onPasswordCopy = {
                                            val password = viewModel.copyPassword(chainLink).value

                                            clipboardManager.setText(AnnotatedString(password))

                                            coroutineScope.launch {
                                                popupMessageState.value = "Password copied"
                                                isPopupVisibleState.value = true

                                                delay(1000L)

                                                popupMessageState.value = ""
                                                isPopupVisibleState.value = false
                                            }
                                        }
                                    )
                                }
                                ChainLink.Status.DRAFT -> key(chainLink.id) {
                                    ChainLinkListItemDraft(
                                        chainLink = chainLink,
                                        onNew = { viewModel.new(chainLink) },
                                        onCancel = { viewModel.rejectDraft(chainLink) }
                                    )
                                }
                                ChainLink.Status.EDIT -> key(chainLink.id) {
                                    ChainLinkListItemEdit(
                                        chainLink = chainLink,
                                        onEdit = { viewModel.edit(chainLink) },
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
        }
    }
}