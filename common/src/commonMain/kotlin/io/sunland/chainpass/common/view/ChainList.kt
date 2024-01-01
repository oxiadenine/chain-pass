package io.sunland.chainpass.common.view

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
fun ChainList(
    viewModel: ChainListViewModel,
    settingsState: MutableState<Settings>,
    navigationState: NavigationState,
    snackbarHostState: SnackbarHostState,
    popupHostState: PopupHostState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val chainListActionState = remember { mutableStateOf(ChainListAction.SELECT) }

    val isLoadingIndicatorVisibleState = remember { mutableStateOf(false) }
    val isInputDialogVisibleState = remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        isLoadingIndicatorVisibleState.value = true

        viewModel.getAll()

        isLoadingIndicatorVisibleState.value = false
    }

    Column(modifier = modifier) {
        ChainListTopBar(
            title = "Chain Pass",
            onSettings = {
                snackbarHostState.currentSnackbarData?.dismiss()

                navigationState.screenState.value = Screen.SETTINGS
            },
            onSync = {
                snackbarHostState.currentSnackbarData?.performAction()

                coroutineScope.launch(Dispatchers.IO) {
                    if (settingsState.value.deviceAddress.isEmpty()) {
                        popupHostState.currentPopupData?.dismiss()
                        popupHostState.showPopup(message = "You have to set Device Address on Settings")
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

        Box(modifier = Modifier.fillMaxSize()) {
            val lazyListState = rememberLazyListState()

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
                    snackbarHostState.currentSnackbarData?.dismiss()

                    isInputDialogVisibleState.value = false

                    coroutineScope.launch(Dispatchers.IO) {
                        isLoadingIndicatorVisibleState.value = true

                        val chain = viewModel.chainSelected!!.apply { key = chainKey }

                        viewModel.select(chain).onSuccess {
                            isLoadingIndicatorVisibleState.value = false

                            navigationState.chainState.value = chain
                            navigationState.screenState.value = Screen.CHAIN_LINK_LIST
                        }.onFailure { exception ->
                            isLoadingIndicatorVisibleState.value = false

                            popupHostState.currentPopupData?.dismiss()
                            popupHostState.showPopup(message = exception.message ?: "Error")
                        }
                    }
                },
                onCancel = { isInputDialogVisibleState.value = false }
            )
            ChainListAction.REMOVE -> ChainListItemKeyInput(
                onKey = { chainKey ->
                    snackbarHostState.currentSnackbarData?.dismiss()

                    isInputDialogVisibleState.value = false

                    coroutineScope.launch(Dispatchers.IO) {
                        val chain = viewModel.chainSelected!!.apply { key = chainKey }

                        viewModel.removeLater(chain)

                        when (snackbarHostState.showSnackbar(
                            message = "${chain.name.value} removed",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short
                        )) {
                            SnackbarResult.ActionPerformed -> viewModel.undoRemove(chain)
                            SnackbarResult.Dismissed -> viewModel.remove(chain).onFailure { exception ->
                                viewModel.undoRemove(chain)

                                popupHostState.currentPopupData?.dismiss()
                                popupHostState.showPopup(message = exception.message ?: "Error")
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
            ChainListAction.UNSTORE -> ChainListUnstoreInput(
                isSingle = false,
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
            ChainListAction.NONE -> Unit
        }
    }

    if (isLoadingIndicatorVisibleState.value) {
        LoadingIndicator()
    }
}