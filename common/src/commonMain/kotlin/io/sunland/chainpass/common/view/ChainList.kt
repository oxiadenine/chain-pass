package io.sunland.chainpass.common.view

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.component.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ChainList(
    viewModel: ChainListViewModel,
    onTopAppBarMenuClick: () -> Unit,
    onListItemOpenMenuItemClick: (Chain) -> Unit,
    deviceAddress: String
) {
    val coroutineScope = rememberCoroutineScope()

    val scaffoldListState = rememberScaffoldListState()

    var listItemMenuItem by remember { mutableStateOf<ChainListItemMenuItem?>(null) }

    var listItemKeyDialogVisible by remember { mutableStateOf(false) }
    var listItemNewDialogVisible by remember { mutableStateOf(false) }

    var storeDialogVisible by remember { mutableStateOf(false) }
    var unstoreDialogVisible by remember { mutableStateOf(false) }

    var loadingDialogVisible by remember { mutableStateOf(false) }

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
            ChainListTopAppBar(
                onMenuClick = {
                    scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                    onTopAppBarMenuClick()
                },
                onMenuItemClick = { menuItem ->
                    when (menuItem) {
                        ChainListTopAppBarMenuItem.SYNC -> {
                            scaffoldListState.snackbarHostState.currentSnackbarData?.performAction()

                            coroutineScope.launch(Dispatchers.IO) {
                                if (deviceAddress.isEmpty()) {
                                    scaffoldListState.popupHostState.currentPopupData?.dismiss()
                                    scaffoldListState.popupHostState.showPopup(
                                        message = "Set device address in Settings"
                                    )
                                } else {
                                    loadingDialogVisible = true

                                    viewModel.sync(deviceAddress).onSuccess {
                                        viewModel.getAll()

                                        loadingDialogVisible = false
                                    }.onFailure { exception ->
                                        loadingDialogVisible = false

                                        scaffoldListState.popupHostState.currentPopupData?.dismiss()
                                        scaffoldListState.popupHostState.showPopup(message = exception.message ?: "Error")
                                    }
                                }
                            }
                        }
                        ChainListTopAppBarMenuItem.STORE -> storeDialogVisible = true
                        ChainListTopAppBarMenuItem.UNSTORE -> unstoreDialogVisible = true
                    }
                },
                title = "Chain Pass",
            )
        },
        floatingActionButton = {
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
                    enter = slideInVertically(animationSpec = tween(durationMillis = 250)) {
                        with(density) { 80.dp.roundToPx() }
                    },
                    exit = slideOutVertically(animationSpec = tween(durationMillis = 250)) {
                        with(density) { 80.dp.roundToPx() }
                    }
                ) {
                    FloatingActionButton(
                        onClick = { listItemNewDialogVisible = true },
                        modifier = Modifier
                            .padding(end = 16.dp, bottom = if (isSnackbarVisible) 80.dp else 16.dp)
                            .pointerHoverIcon(icon = PointerIconDefaults.Hand)
                    ) { Icon(imageVector = Icons.Default.Add, contentDescription = null) }
                }
            }
        }
    ) { lazyListState ->
        if (viewModel.chainListState.isEmpty()) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "New Chain")
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        } else {
            LazyColumn(state = lazyListState, modifier = Modifier.fillMaxWidth()) {
                items(items = viewModel.chainListState.toTypedArray(), key = { chain -> chain.id }) { chain ->
                    if (!chain.isDraft) {
                        ChainListItem(
                            onMenuItemClick = { menuItem ->
                                viewModel.selectForKey(chain)

                                listItemMenuItem = menuItem
                                listItemKeyDialogVisible = true
                            },
                            name = chain.name.value
                        )
                    } else ChainListItemDraft(name = chain.name.value)
                }
            }

            viewModel.chainSelectedIndex.takeIf { index -> index != -1 }?.let { index ->
                LaunchedEffect(index) { lazyListState.animateScrollToItem(index) }
            }
        }
    }

    if (listItemKeyDialogVisible) {
        ChainListItemKeyDialog(
            onConfirm = { chainKey ->
                listItemKeyDialogVisible = false

                scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                when (listItemMenuItem!!) {
                    ChainListItemMenuItem.OPEN -> coroutineScope.launch(Dispatchers.IO) {
                        loadingDialogVisible = true

                        viewModel.select(chainKey).onSuccess {
                            loadingDialogVisible = false

                            onListItemOpenMenuItemClick(viewModel.chainSelected!!)
                        }.onFailure { exception ->
                            loadingDialogVisible = false

                            scaffoldListState.popupHostState.currentPopupData?.dismiss()
                            scaffoldListState.popupHostState.showPopup(message = exception.message ?: "Error")
                        }
                    }
                    ChainListItemMenuItem.DELETE -> coroutineScope.launch(Dispatchers.IO) {
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
                }
            },
            onCancel = { listItemKeyDialogVisible = false }
        )
    }

    if (listItemNewDialogVisible) {
        ChainListItemNewDialog(
            onConfirm = { chainName, chainKey ->
                listItemNewDialogVisible = false

                coroutineScope.launch(Dispatchers.IO) {
                    viewModel.new(chainName, chainKey)
                }
            },
            onCancel = { listItemNewDialogVisible = false },
            passwordGenerator = viewModel.passwordGenerator
        )
    }

    if (storeDialogVisible) {
        StoreDialog(
            onConfirm = { storageType, storeIsPrivate ->
                storeDialogVisible = false

                scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                coroutineScope.launch(Dispatchers.IO) {
                    loadingDialogVisible = true

                    viewModel.store(storageType, storeIsPrivate).onSuccess { fileName ->
                        loadingDialogVisible = false

                        scaffoldListState.popupHostState.currentPopupData?.dismiss()
                        scaffoldListState.popupHostState.showPopup(message = "Stored to $fileName")
                    }.onFailure { exception ->
                        loadingDialogVisible = false

                        scaffoldListState.popupHostState.currentPopupData?.dismiss()
                        scaffoldListState.popupHostState.showPopup(message = exception.message ?: "Error")
                    }
                }
            },
            onCancel = { storeDialogVisible = false },
            title = "Store",
            isSingle = false
        )
    }

    if (unstoreDialogVisible) {
        UnstoreDialog(
            onConfirm = { filePath ->
                unstoreDialogVisible = false

                scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                coroutineScope.launch(Dispatchers.IO) {
                    loadingDialogVisible = true

                    viewModel.unstore(filePath).onSuccess {
                        viewModel.getAll()

                        loadingDialogVisible = false

                        scaffoldListState.popupHostState.currentPopupData?.dismiss()
                        scaffoldListState.popupHostState.showPopup(message = "Unstored from ${filePath.fileName}")
                    }.onFailure { exception ->
                        loadingDialogVisible = false

                        scaffoldListState.popupHostState.currentPopupData?.dismiss()
                        scaffoldListState.popupHostState.showPopup(message = exception.message ?: "Error")
                    }
                }
            },
            onCancel = { unstoreDialogVisible = false },
            title = "Unstore",
            isSingle = false
        )
    }

    if (loadingDialogVisible) {
        LoadingDialog()
    }

    LaunchedEffect(viewModel) {
        loadingDialogVisible = true

        viewModel.getAll()

        loadingDialogVisible = false
    }
}