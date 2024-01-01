package io.sunland.chainpass.common.view

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.RectangleShape
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
fun ChainLinkScaffoldList(
    viewModel: ChainLinkListViewModel,
    settingsState: SettingsState,
    navigationState: NavigationState,
) {
    val coroutineScope = rememberCoroutineScope()

    val scaffoldListState = rememberScaffoldListState()

    val chainLinkListActionState = remember { mutableStateOf(ChainLinkListAction.NONE) }

    val isDialogVisibleState = remember { mutableStateOf(false) }
    val isLoadingIndicatorVisibleState = remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        isLoadingIndicatorVisibleState.value = true

        viewModel.getAll()

        isLoadingIndicatorVisibleState.value = false
    }

    ScaffoldList(
        scaffoldListState = scaffoldListState,
        popupHost = { popupHostState ->
            PopupHost(hostState = popupHostState) { popupData ->
                Text(
                    text = popupData.message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        snackbarHost = { snackbarHostState ->
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        modifier = Modifier.padding(all = 16.dp),
                        action = {
                            snackbarData.visuals.actionLabel?.let { label ->
                                TextButton(
                                    onClick = { snackbarHostState.currentSnackbarData?.performAction() },
                                    modifier = Modifier.pointerHoverIcon(PointerIconDefaults.Hand)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Undo,
                                        contentDescription = null,
                                        modifier = Modifier.rotate(degrees = 90f)
                                    )
                                    Spacer(modifier = Modifier.size(size = ButtonDefaults.IconSpacing))
                                    Text(text = label, fontSize = 14.sp)
                                }
                            }
                        },
                        shape = if (platform == Platform.DESKTOP) RectangleShape else SnackbarDefaults.shape,
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(elevation = 4.dp),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) { Text(text = snackbarData.visuals.message, fontSize = 14.sp) }
                }
            )
        },
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (viewModel.isSearchState.value) {
                ChainLinkSearchListTopBar(
                    keywordState = viewModel.searchKeywordState,
                    onBack = { viewModel.endSearch() },
                    onSearch = { keyword -> viewModel.search(keyword) }
                )
            } else {
                ChainLinkListTopBar(
                    title = viewModel.chain!!.name.value,
                    onBack = {
                        scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                        viewModel.back()

                        navigationState.screenState.value = Screen.CHAIN_LIST
                    },
                    onSync = {
                        scaffoldListState.snackbarHostState.currentSnackbarData?.performAction()

                        coroutineScope.launch(Dispatchers.IO) {
                            if (settingsState.deviceAddressState.value.isEmpty()) {
                                scaffoldListState.popupHostState.currentPopupData?.dismiss()
                                scaffoldListState.popupHostState.showPopup(message = "You have to set sync options on Settings")
                            } else {
                                isLoadingIndicatorVisibleState.value = true

                                viewModel.sync(settingsState.deviceAddressState.value).onSuccess {
                                    viewModel.getAll()

                                    isLoadingIndicatorVisibleState.value = false
                                }.onFailure { exception ->
                                    isLoadingIndicatorVisibleState.value = false

                                    scaffoldListState.popupHostState.currentPopupData?.dismiss()
                                    scaffoldListState.popupHostState.showPopup(message = exception.message ?: "Error")
                                }
                            }
                        }
                    },
                    onSearch = {
                        scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()
                        scaffoldListState.popupHostState.currentPopupData?.dismiss()

                        viewModel.startSearch()
                    },
                    onStore = {
                        chainLinkListActionState.value = ChainLinkListAction.STORE
                        isDialogVisibleState.value = true
                    },
                    onUnstore = {
                        chainLinkListActionState.value = ChainLinkListAction.UNSTORE
                        isDialogVisibleState.value = true
                    }
                )
            }
        },
        floatingButton = {
            AnimatedContent(
                targetState = scaffoldListState.snackbarHostState.isSnackbarVisible(),
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
                val density = LocalDensity.current

                val (lazyListScrollDirection, lazyListScrollPosition) = scaffoldListState.lazyListState.scrollInfo()

                AnimatedVisibility(
                    visible = !viewModel.isSearchState.value && (isSnackbarVisible ||
                            lazyListScrollDirection == LazyListScrollDirection.BACKWARD ||
                            lazyListScrollPosition == LazyListScrollPosition.END),
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
                            isDialogVisibleState.value = true
                        },
                        modifier = Modifier
                            .padding(end = 16.dp, bottom = if (isSnackbarVisible) 80.dp else 16.dp)
                            .pointerHoverIcon(icon = PointerIconDefaults.Hand)
                    ) { Icon(imageVector = Icons.Default.Add, contentDescription = null) }
                }
            }
        }
    ) { lazyListState ->
        val chainLinks = if (viewModel.isSearchState.value) {
            viewModel.chainLinkSearchListState.toTypedArray()
        } else viewModel.chainLinkListState.toTypedArray()

        if (chainLinks.isEmpty()) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                verticalAlignment = Alignment.CenterVertically
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
                        ChainLinkSearchListItem(chainLink = chainLink, onSelect = { viewModel.endSearch(chainLink) })
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
                                    isDialogVisibleState.value = true
                                }
                            },
                            onRemove = {
                                scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                                coroutineScope.launch(Dispatchers.IO) {
                                    viewModel.removeLater(chainLink)

                                    when (scaffoldListState.snackbarHostState.showSnackbar(
                                        message = "${chainLink.name.value} removed",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )) {
                                        SnackbarResult.ActionPerformed -> viewModel.undoRemove()
                                        SnackbarResult.Dismissed -> viewModel.remove()
                                    }
                                }
                            },
                            onPasswordCopy = {
                                val password = viewModel.copyPassword(chainLink).value

                                clipboardManager.setText(AnnotatedString(password))

                                scaffoldListState.popupHostState.currentPopupData?.dismiss()

                                coroutineScope.launch(Dispatchers.IO) {
                                    scaffoldListState.popupHostState.showPopup(message = "Password copied")
                                }
                            }
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
                    lazyListState.animateScrollToItem(index)
                }
            }
        }

        if (isDialogVisibleState.value) {
            when (chainLinkListActionState.value) {
                ChainLinkListAction.NEW -> {
                    ChainLinkListItemNewDialog(
                        chainLink = viewModel.draft(),
                        onNew = { chainLink ->
                            isDialogVisibleState.value = false

                            coroutineScope.launch(Dispatchers.IO) {
                                viewModel.new(chainLink)
                            }
                        },
                        onCancel = { isDialogVisibleState.value = false }
                    )
                }
                ChainLinkListAction.EDIT -> {
                    ChainLinkListItemEditDialog(
                        chainLink = viewModel.chainLinkEdited!!,
                        onEdit = {
                            isDialogVisibleState.value = false

                            coroutineScope.launch(Dispatchers.IO) {
                                isLoadingIndicatorVisibleState.value = true

                                viewModel.edit()

                                isLoadingIndicatorVisibleState.value = false
                            }
                        },
                        onCancel = {
                            isDialogVisibleState.value = false

                            viewModel.cancelEdit()
                        }
                    )
                }
                ChainLinkListAction.STORE -> ChainListStoreDialog(
                    isSingle = true,
                    onStore = { storeOptions ->
                        scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                        isDialogVisibleState.value = false

                        coroutineScope.launch(Dispatchers.IO) {
                            isLoadingIndicatorVisibleState.value = true

                            viewModel.store(storeOptions).onSuccess { fileName ->
                                isLoadingIndicatorVisibleState.value = false

                                scaffoldListState.popupHostState.currentPopupData?.dismiss()
                                scaffoldListState.popupHostState.showPopup(message = "Stored to $fileName")
                            }.onFailure { exception ->
                                isLoadingIndicatorVisibleState.value = false

                                scaffoldListState.popupHostState.currentPopupData?.dismiss()
                                scaffoldListState.popupHostState.showPopup(message = exception.message ?: "Error")
                            }
                        }
                    },
                    onCancel = { isDialogVisibleState.value = false }
                )
                ChainLinkListAction.UNSTORE -> ChainListUnstoreDialog(
                    isSingle = true,
                    onUnstore = { filePath ->
                        scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                        isDialogVisibleState.value = false

                        coroutineScope.launch(Dispatchers.IO) {
                            isLoadingIndicatorVisibleState.value = true

                            viewModel.unstore(filePath).onSuccess {
                                viewModel.getAll()

                                isLoadingIndicatorVisibleState.value = false

                                scaffoldListState.popupHostState.currentPopupData?.dismiss()
                                scaffoldListState.popupHostState.showPopup(message = "Unstored from ${filePath.fileName}")
                            }.onFailure { exception ->
                                isLoadingIndicatorVisibleState.value = false

                                scaffoldListState.popupHostState.currentPopupData?.dismiss()
                                scaffoldListState.popupHostState.showPopup(message = exception.message ?: "Error")
                            }
                        }
                    },
                    onCancel = { isDialogVisibleState.value = false }
                )
                ChainLinkListAction.NONE -> Unit
            }
        }

        if (isLoadingIndicatorVisibleState.value) {
            LoadingDialog()
        }
    }
}