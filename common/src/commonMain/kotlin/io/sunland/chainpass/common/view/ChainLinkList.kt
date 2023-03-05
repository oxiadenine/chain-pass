package io.sunland.chainpass.common.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Undo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.component.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class ChainLinkListAction { NONE, NEW, EDIT, STORE, UNSTORE }

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkList(
    viewModel: ChainLinkListViewModel,
    settingsState: MutableState<Settings>,
    navigationState: NavigationState,
    snackbarHostState: SnackbarHostState,
    popupHostState: PopupHostState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val chainLinkListActionState = remember { mutableStateOf(ChainLinkListAction.NONE) }

    val isLoadingIndicatorVisibleState = remember { mutableStateOf(false) }
    val isInputDialogVisibleState = remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        isLoadingIndicatorVisibleState.value = true

        viewModel.getAll()

        isLoadingIndicatorVisibleState.value = false
    }

    Column(modifier = modifier) {
        if (viewModel.isSearchState.value) {
            ChainLinkSearchListTopBar(
                keywordState = viewModel.searchKeywordState,
                onBack = { viewModel.endSearch() },
                onSearch = { keyword -> viewModel.search(keyword) },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            ChainLinkListTopBar(
                title = viewModel.chain!!.name.value,
                onBack = {
                    snackbarHostState.currentSnackbarData?.dismiss()

                    viewModel.back()

                    navigationState.screenState.value = Screen.CHAIN_LIST
                },
                onSync = {
                    snackbarHostState.currentSnackbarData?.performAction()

                    coroutineScope.launch(Dispatchers.IO) {
                        if (settingsState.value.deviceAddress.isEmpty()) {
                            popupHostState.currentPopupData?.dismiss()
                            popupHostState.showPopup(message = "You have to set sync options on Settings")
                        } else {
                            isLoadingIndicatorVisibleState.value = true

                            viewModel.sync(settingsState.value.deviceAddress).onSuccess {
                                viewModel.getAll()

                                isLoadingIndicatorVisibleState.value = false
                            }.onFailure { exception ->
                                isLoadingIndicatorVisibleState.value = false

                                popupHostState.currentPopupData?.dismiss()
                                popupHostState.showPopup(message = exception.message ?: "Error")
                            }
                        }
                    }
                },
                onSearch = {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    popupHostState.currentPopupData?.dismiss()

                    viewModel.startSearch()
                },
                onStore = {
                    chainLinkListActionState.value = ChainLinkListAction.STORE
                    isInputDialogVisibleState.value = true
                },
                onUnstore = {
                    chainLinkListActionState.value = ChainLinkListAction.UNSTORE
                    isInputDialogVisibleState.value = true
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val lazyListState = rememberLazyListState()

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
                LazyColumn(state = lazyListState, modifier = Modifier.fillMaxWidth()) {
                    items(items = chainLinks, key = { chainLink -> chainLink.id }) { chainLink ->
                        if (viewModel.isSearchState.value) {
                            ChainLinkSearchListItem(
                                chainLink = chainLink,
                                onSelect = { viewModel.endSearch(chainLink) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            val clipboardManager = LocalClipboardManager.current

                            ChainLinkListItem(
                                chainLink = chainLink,
                                onEdit = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        isLoadingIndicatorVisibleState.value = true

                                        viewModel.startEdit(chainLink)

                                        isLoadingIndicatorVisibleState.value = false

                                        chainLinkListActionState.value = ChainLinkListAction.EDIT
                                        isInputDialogVisibleState.value = true
                                    }
                                },
                                onDelete = {
                                    snackbarHostState.currentSnackbarData?.dismiss()

                                    coroutineScope.launch(Dispatchers.IO) {
                                        viewModel.removeLater(chainLink)

                                        when (snackbarHostState.showSnackbar(
                                            message = "${chainLink.name.value} removed",
                                            actionLabel = "Undo",
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

                                    popupHostState.currentPopupData?.dismiss()

                                    coroutineScope.launch(Dispatchers.IO) {
                                        popupHostState.showPopup(message = "Password copied")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                LaunchedEffect(
                    viewModel.isSearchState.value,
                    viewModel.searchKeywordState.value,
                    viewModel.chainLinkSearchListState.size
                ) { lazyListState.animateScrollToItem(0) }

                viewModel.chainLinkSelectedIndex.takeIf { index -> index != -1 }?.let { index ->
                    LaunchedEffect(index) {
                        println("search")
                        lazyListState.animateScrollToItem(index)
                    }
                }
            }

            if (!viewModel.isSearchState.value) {
                val density = LocalDensity.current

                Column(modifier = Modifier.align(alignment = Alignment.BottomEnd)) {
                    AnimatedContent(
                        targetState = snackbarHostState.isSnackbarVisible(),
                        transitionSpec = {
                            if (targetState && !initialState) {
                                slideInVertically(
                                    initialOffsetY = { 0 },
                                    animationSpec = tween(easing = LinearEasing, durationMillis = 150)
                                ) with ExitTransition.None
                            } else slideInVertically(
                                initialOffsetY = { 0 },
                                animationSpec = tween(easing = LinearEasing, durationMillis = 75)
                            ) with ExitTransition.None
                        }
                    ) { isSnackbarVisible ->
                        AnimatedVisibility(
                            visible = isSnackbarVisible ||
                                    lazyListState.scrollDirection() == LazyListScrollDirection.BACKWARD,
                            enter = slideInVertically {
                                with(density) { -16.dp.roundToPx() }
                            } + expandVertically(expandFrom = Alignment.Top),
                            exit = slideOutVertically {
                                with(density) { 16.dp.roundToPx() }
                            } + shrinkVertically(shrinkTowards = Alignment.Top)
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    chainLinkListActionState.value = ChainLinkListAction.NEW
                                    isInputDialogVisibleState.value = true
                                },
                                modifier = Modifier
                                    .padding(end = 16.dp, bottom = if (isSnackbarVisible) 80.dp else 16.dp)
                                    .pointerHoverIcon(icon = PointerIconDefaults.Hand),
                                backgroundColor = MaterialTheme.colors.surface,
                            ) { Icon(imageVector = Icons.Default.Add, contentDescription = null) }
                        }
                    }
                }
            }

            PopupHost(hostState = popupHostState) { popupData ->
                Surface(modifier = Modifier.padding(horizontal = 16.dp), elevation = 4.dp) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        text = popupData.message,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(alignment = Alignment.BottomEnd),
                snackbar = { snackbarData ->
                    Snackbar(
                        modifier = Modifier.padding(all = 16.dp),
                        content = { Text(text = snackbarData.message, fontSize = 14.sp) },
                        action = {
                            snackbarData.actionLabel?.let { label ->
                                TextButton(
                                    onClick = { snackbarHostState.currentSnackbarData?.performAction() },
                                    modifier = Modifier.pointerHoverIcon(PointerIconDefaults.Hand),
                                ) {
                                    Icon(
                                        modifier = Modifier.rotate(degrees = 90f),
                                        imageVector = Icons.Default.Undo,
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.size(size = ButtonDefaults.IconSpacing))
                                    Text(text = label, fontSize = 14.sp)
                                }
                            }
                        },
                        backgroundColor = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.primary
                    )
                }
            )
        }
    }

    if (isInputDialogVisibleState.value) {
        when (chainLinkListActionState.value) {
            ChainLinkListAction.NEW -> {
                val chainLink = viewModel.draft()

                ChainLinkListItemNewInput(
                    chainLink = chainLink,
                    onNew = {
                        isInputDialogVisibleState.value = false

                        coroutineScope.launch(Dispatchers.IO) {
                            isLoadingIndicatorVisibleState.value = true

                            viewModel.new(chainLink)

                            isLoadingIndicatorVisibleState.value = false
                        }
                    },
                    onCancel = { isInputDialogVisibleState.value = false }
                )
            }
            ChainLinkListAction.EDIT -> {
                val chainLink = viewModel.chainLinkSelected!!

                ChainLinkListItemEditInput(
                    chainLink = chainLink,
                    onEdit = {
                        isInputDialogVisibleState.value = false

                        coroutineScope.launch(Dispatchers.IO) {
                            isLoadingIndicatorVisibleState.value = true

                            viewModel.edit(chainLink)

                            isLoadingIndicatorVisibleState.value = false
                        }
                    },
                    onCancel = {
                        isInputDialogVisibleState.value = false

                        viewModel.cancelEdit(chainLink)
                    }
                )
            }
            ChainLinkListAction.STORE -> ChainListStoreInput(
                isSingle = true,
                onStore = { storeOptions ->
                    snackbarHostState.currentSnackbarData?.dismiss()

                    isInputDialogVisibleState.value = false

                    coroutineScope.launch(Dispatchers.IO) {
                        isLoadingIndicatorVisibleState.value = true

                        viewModel.store(storeOptions).onSuccess { fileName ->
                            isLoadingIndicatorVisibleState.value = false

                            popupHostState.currentPopupData?.dismiss()
                            popupHostState.showPopup(message = "Stored to $fileName")
                        }.onFailure { exception ->
                            isLoadingIndicatorVisibleState.value = false

                            popupHostState.currentPopupData?.dismiss()
                            popupHostState.showPopup(message = exception.message ?: "Error")
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

                    coroutineScope.launch(Dispatchers.IO) {
                        isLoadingIndicatorVisibleState.value = true

                        viewModel.unstore(filePath).onSuccess {
                            viewModel.getAll()

                            isLoadingIndicatorVisibleState.value = false

                            popupHostState.currentPopupData?.dismiss()
                            popupHostState.showPopup(message = "Unstored from ${filePath.fileName}")
                        }.onFailure { exception ->
                            isLoadingIndicatorVisibleState.value = false

                            popupHostState.currentPopupData?.dismiss()
                            popupHostState.showPopup(message = exception.message ?: "Error")
                        }
                    }
                },
                onCancel = { isInputDialogVisibleState.value = false }
            )
            ChainLinkListAction.NONE -> Unit
        }
    }

    if (isLoadingIndicatorVisibleState.value) {
        LoadingIndicator()
    }
}