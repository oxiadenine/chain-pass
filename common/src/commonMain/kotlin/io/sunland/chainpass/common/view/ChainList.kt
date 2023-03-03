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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.NavigationState
import io.sunland.chainpass.common.Screen
import io.sunland.chainpass.common.Settings
import io.sunland.chainpass.common.component.LazyListScrollDirection
import io.sunland.chainpass.common.component.scrollDirection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class ChainListAction { NONE, NEW, SELECT, REMOVE, STORE, UNSTORE }

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
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

    val isSnackbarVisibleState = remember {
        snapshotFlow { snackbarHostState.currentSnackbarData != null }
    }.collectAsState(false)

    if (isWorkInProgressState.value) {
        LoadingIndicator()
    }

    if (isInputDialogVisibleState.value) {
        when (chainListActionState.value) {
            ChainListAction.NEW -> {
                val chain = viewModel.draft()

                ChainListItemNewInput(
                    chain = chain,
                    onNew = {
                        isInputDialogVisibleState.value = false

                        coroutineScope.launch(Dispatchers.IO) {
                            isWorkInProgressState.value = true

                            viewModel.new(chain)

                            isWorkInProgressState.value = false
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
                        isWorkInProgressState.value = true

                        val chain = viewModel.chainSelected!!.apply { key = chainKey }

                        viewModel.setSelected()
                        viewModel.select(chain).onSuccess {
                            isWorkInProgressState.value = false

                            navigationState.chainState.value = chain
                            navigationState.screenState.value = Screen.CHAIN_LINK_LIST
                        }.onFailure { exception ->
                            isWorkInProgressState.value = false

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

                    coroutineScope.launch(Dispatchers.IO) {
                        val chain = viewModel.chainSelected!!.apply { key = chainKey }

                        viewModel.removeLater(chain)

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

                    coroutineScope.launch(Dispatchers.IO) {
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

                    coroutineScope.launch(Dispatchers.IO) {
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

    LaunchedEffect(Unit) {
        isWorkInProgressState.value = true

        viewModel.getAll()

        isWorkInProgressState.value = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ChainListTopBar(
            title = "Chain Pass",
            onSettings = { navigationState.screenState.value = Screen.SETTINGS },
            onSync = {
                snackbarHostState.currentSnackbarData?.performAction()

                coroutineScope.launch(Dispatchers.IO) {
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
                LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {
                    items(items = viewModel.chainListState.toTypedArray(), key = { chain -> chain.id }) { chain ->
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
                }

                viewModel.chainLatestIndex.takeIf { index -> index != -1 }?.let { index ->
                    LaunchedEffect(index) { lazyListState.animateScrollToItem(index) }
                }
            }

            val density = LocalDensity.current

            Column(modifier = Modifier.align(alignment = Alignment.BottomEnd)) {
                AnimatedContent(
                    targetState = isSnackbarVisibleState.value,
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
        }
    }
}