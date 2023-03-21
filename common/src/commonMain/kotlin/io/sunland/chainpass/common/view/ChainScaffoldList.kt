package io.sunland.chainpass.common.view

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Undo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.NavigationState
import io.sunland.chainpass.common.Screen
import io.sunland.chainpass.common.Settings
import io.sunland.chainpass.common.component.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class ChainListAction { NONE, NEW, SELECT, REMOVE, STORE, UNSTORE }

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun ChainScaffoldList(
    viewModel: ChainListViewModel,
    settingsState: MutableState<Settings>,
    navigationState: NavigationState,
    scaffoldListState: ScaffoldListState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val chainListActionState = remember { mutableStateOf(ChainListAction.SELECT) }

    val isInputDialogVisibleState = remember { mutableStateOf(false) }
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
                Surface(modifier = Modifier.padding(horizontal = 16.dp), elevation = 4.dp) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        text = popupData.message,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        },
        snackbarHost = { snackbarHostState ->
            SnackbarHost(
                hostState = snackbarHostState,
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
        },
        modifier = modifier,
        topBar = {
            ChainListTopBar(
                title = "Chain Pass",
                onSettings = {
                    scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                    navigationState.screenState.value = Screen.SETTINGS
                },
                onSync = {
                    scaffoldListState.snackbarHostState.currentSnackbarData?.performAction()

                    coroutineScope.launch(Dispatchers.IO) {
                        if (settingsState.value.deviceAddress.isEmpty()) {
                            scaffoldListState.popupHostState.currentPopupData?.dismiss()
                            scaffoldListState.popupHostState.showPopup(
                                message = "You have to set Device Address on Settings"
                            )
                        } else {
                            isLoadingIndicatorVisibleState.value = true

                            viewModel.sync(settingsState.value.deviceAddress).onSuccess {
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
                onStore = {
                    chainListActionState.value = ChainListAction.STORE
                    isInputDialogVisibleState.value = true
                },
                onUnstore = {
                    chainListActionState.value = ChainListAction.UNSTORE
                    isInputDialogVisibleState.value = true
                },
                modifier = Modifier.fillMaxWidth()
            )
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
                    visible = isSnackbarVisible ||
                            lazyListScrollDirection == LazyListScrollDirection.BACKWARD ||
                            lazyListScrollPosition == LazyListScrollPosition.END,
                    enter = slideInVertically {
                        with(density) { -16.dp.roundToPx() }
                    } + expandVertically(expandFrom = Alignment.Top),
                    exit = slideOutVertically {
                        with(density) { 16.dp.roundToPx() }
                    } + shrinkVertically(shrinkTowards = Alignment.Top)
                ) {
                    FloatingActionButton(
                        onClick = {
                            chainListActionState.value = ChainListAction.NEW
                            isInputDialogVisibleState.value = true
                        },
                        modifier = Modifier
                            .padding(end = 16.dp, bottom = if (isSnackbarVisible) 80.dp else 16.dp)
                            .pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        backgroundColor = MaterialTheme.colors.background
                    ) { Icon(imageVector = Icons.Default.Add, contentDescription = null) }
                }
            }
        }
    ) { lazyListState ->
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
            LazyColumn(state = lazyListState, modifier = Modifier.fillMaxWidth()) {
                items(items = viewModel.chainListState.toTypedArray(), key = { chain -> chain.id }) { chain ->
                    ChainListItem(
                        chain = chain,
                        onSelect = {
                            viewModel.selectForKey(chain)

                            chainListActionState.value = ChainListAction.SELECT
                            isInputDialogVisibleState.value = true
                        },
                        onRemove = {
                            viewModel.selectForKey(chain)

                            chainListActionState.value = ChainListAction.REMOVE
                            isInputDialogVisibleState.value = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            viewModel.chainSelectedIndex.takeIf { index -> index != -1 }?.let { index ->
                LaunchedEffect(index) { lazyListState.animateScrollToItem(index) }
            }
        }

        if (isInputDialogVisibleState.value) {
            when (chainListActionState.value) {
                ChainListAction.NEW -> {
                    ChainListItemNewInput(
                        chain = viewModel.draft(),
                        onNew = { chain ->
                            isInputDialogVisibleState.value = false

                            coroutineScope.launch(Dispatchers.IO) {
                                isLoadingIndicatorVisibleState.value = true

                                viewModel.new(chain)

                                isLoadingIndicatorVisibleState.value = false
                            }
                        },
                        onCancel = { isInputDialogVisibleState.value = false }
                    )
                }
                ChainListAction.SELECT -> ChainListItemKeyInput(
                    onKey = { chainKey ->
                        scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                        isInputDialogVisibleState.value = false

                        coroutineScope.launch(Dispatchers.IO) {
                            isLoadingIndicatorVisibleState.value = true

                            viewModel.select(chainKey).onSuccess {
                                isLoadingIndicatorVisibleState.value = false

                                navigationState.chainState.value = viewModel.chainSelected!!
                                navigationState.screenState.value = Screen.CHAIN_LINK_LIST
                            }.onFailure { exception ->
                                isLoadingIndicatorVisibleState.value = false

                                scaffoldListState.popupHostState.currentPopupData?.dismiss()
                                scaffoldListState.popupHostState.showPopup(message = exception.message ?: "Error")
                            }
                        }
                    },
                    onCancel = { isInputDialogVisibleState.value = false }
                )
                ChainListAction.REMOVE -> ChainListItemKeyInput(
                    onKey = { chainKey ->
                        scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                        isInputDialogVisibleState.value = false

                        coroutineScope.launch(Dispatchers.IO) {
                            viewModel.removeLater(chainKey)

                            when (scaffoldListState.snackbarHostState.showSnackbar(
                                message = "${viewModel.chainSelected!!.name.value} removed",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )) {
                                SnackbarResult.ActionPerformed -> viewModel.undoRemove()
                                SnackbarResult.Dismissed -> viewModel.remove().onFailure { exception ->
                                    viewModel.undoRemove()

                                    scaffoldListState.popupHostState.currentPopupData?.dismiss()
                                    scaffoldListState.popupHostState.showPopup(message = exception.message ?: "Error")
                                }
                            }
                        }
                    },
                    onCancel = { isInputDialogVisibleState.value = false }
                )
                ChainListAction.STORE -> ChainListStoreInput(
                    isSingle = false,
                    onStore = { storeOptions ->
                        scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                        isInputDialogVisibleState.value = false

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
                    onCancel = { isInputDialogVisibleState.value = false }
                )
                ChainListAction.UNSTORE -> ChainListUnstoreInput(
                    isSingle = false,
                    onUnstore = { filePath ->
                        scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                        isInputDialogVisibleState.value = false

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
                    onCancel = { isInputDialogVisibleState.value = false }
                )
                ChainListAction.NONE -> Unit
            }
        }

        if (isLoadingIndicatorVisibleState.value) {
            LoadingIndicator()
        }
    }
}