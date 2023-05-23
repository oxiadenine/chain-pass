package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.component.*
import io.sunland.chainpass.common.security.PasswordGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChainLinkListRouteArgument(val chain: Chain, val chainLink: ChainLink? = null) : NavigationState.RouteArgument()

@Composable
fun ChainLinkList(
    viewModel: ChainLinkListViewModel,
    onTopAppBarBackClick: () -> Unit,
    onTopAppBarSearchClick: (List<ChainLink>) -> Unit,
    deviceAddress: String,
    passwordGenerator: PasswordGenerator
) {
    val intl = LocalIntl.current

    val coroutineScope = rememberCoroutineScope()

    val scaffoldListState = rememberScaffoldListState()

    var listItemNewDialogVisible by remember { mutableStateOf(false) }
    var listItemEditDialogVisible by remember { mutableStateOf(false) }

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
                                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
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
            ChainLinkListTopAppBar(
                onBackClick = {
                    scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                    onTopAppBarBackClick()
                },
                onSearchClick = {
                    scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()
                    scaffoldListState.popupHostState.currentPopupData?.dismiss()

                    onTopAppBarSearchClick(viewModel.chainLinkListState.toList())
                },
                onMenuItemClick = { menuItem ->
                    when (menuItem) {
                        ChainLinkListTopAppBarMenuItem.SYNC -> {
                            scaffoldListState.snackbarHostState.currentSnackbarData?.performAction()

                            coroutineScope.launch(Dispatchers.IO) {
                                if (deviceAddress.isEmpty()) {
                                    scaffoldListState.popupHostState.currentPopupData?.dismiss()
                                    scaffoldListState.popupHostState.showPopup(
                                        message = intl.translate("popup.sync.device.message")
                                    )
                                } else {
                                    loadingDialogVisible = true

                                    viewModel.sync(deviceAddress).onSuccess {
                                        viewModel.getAll()

                                        loadingDialogVisible = false
                                    }.onFailure { error ->
                                        loadingDialogVisible = false

                                        scaffoldListState.popupHostState.currentPopupData?.dismiss()

                                        scaffoldListState.popupHostState.showPopup(
                                            message = if (error is ChainLinkListViewModel.SyncNetworkError) {
                                                intl.translate("popup.sync.network.error")
                                            } else "Error"
                                        )
                                    }
                                }
                            }
                        }
                        ChainLinkListTopAppBarMenuItem.STORE -> storeDialogVisible = true
                        ChainLinkListTopAppBarMenuItem.UNSTORE -> unstoreDialogVisible = true
                    }
                },
                title = viewModel.chain.name.value,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { listItemNewDialogVisible = true },
                modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
            ) { Icon(imageVector = Icons.Default.Add, contentDescription = null) }
        }
    ) { lazyListState ->
        val chainLinks = viewModel.chainLinkListState.toTypedArray()

        if (chainLinks.isEmpty()) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = intl.translate("list.chainLink.empty.text"))
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        } else {
            LazyColumn(state = lazyListState, modifier = Modifier.fillMaxWidth()) {
                items(items = chainLinks, key = { chainLink -> chainLink.id }) { chainLink ->
                    val clipboardManager = LocalClipboardManager.current

                    ChainLinkListItem(
                        onMenuItemClick = { menuItem ->
                            when (menuItem) {
                                ChainLinkListItemMenuItem.COPY -> {
                                    scaffoldListState.popupHostState.currentPopupData?.dismiss()

                                    coroutineScope.launch(Dispatchers.IO) {
                                        val password = viewModel.copyPassword(chainLink).value

                                        clipboardManager.setText(AnnotatedString(password))

                                        scaffoldListState.popupHostState.showPopup(
                                            message = intl.translate("popup.chainLink.password.message")
                                        )
                                    }
                                }
                                ChainLinkListItemMenuItem.EDIT -> {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        loadingDialogVisible = true

                                        viewModel.startEdit(chainLink)

                                        loadingDialogVisible = false

                                        listItemEditDialogVisible = true
                                    }
                                }
                                ChainLinkListItemMenuItem.DELETE -> {
                                    scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                                    coroutineScope.launch(Dispatchers.IO) {
                                        viewModel.removeLater(chainLink)

                                        when (scaffoldListState.snackbarHostState.showSnackbar(
                                            message = intl.translate(
                                                id = "snackbar.label.delete.text",
                                                value = "name" to chainLink.name.value
                                            ),
                                            actionLabel = intl.translate("snackbar.button.undo.text"),
                                            duration = SnackbarDuration.Short
                                        )) {
                                            SnackbarResult.ActionPerformed -> viewModel.undoRemove()
                                            SnackbarResult.Dismissed -> viewModel.remove()
                                        }
                                    }
                                }
                            }
                        },
                        name = chainLink.name.value,
                        description = chainLink.description.value
                    )
                }
            }

            viewModel.chainLinkSelectedIndex.takeIf { index -> index != -1 }?.let { index ->
                LaunchedEffect(index) {
                    lazyListState.animateScrollToItem(index)
                }
            }
        }
    }

    if (listItemNewDialogVisible) {
        ChainLinkListItemNewDialog(
            onConfirm = { chainLinkName, chainLinkDescription, chainLinkPassword ->
                listItemNewDialogVisible = false

                coroutineScope.launch(Dispatchers.IO) {
                    loadingDialogVisible = true

                    viewModel.new(chainLinkName, chainLinkDescription, chainLinkPassword)

                    loadingDialogVisible = false
                }
            },
            onCancel = { listItemNewDialogVisible = false },
            passwordGenerator = passwordGenerator
        )
    }

    if (listItemEditDialogVisible) {
        ChainLinkListItemEditDialog(
            onConfirm = { chainLinkDescription, chainLinkPassword ->
                listItemEditDialogVisible = false

                coroutineScope.launch(Dispatchers.IO) {
                    loadingDialogVisible = true

                    viewModel.edit(chainLinkDescription, chainLinkPassword)

                    loadingDialogVisible = false
                }
            },
            onCancel = {
                listItemEditDialogVisible = false

                viewModel.cancelEdit()
            },
            chainLink = viewModel.chainLinkEdit!!,
            passwordGenerator = passwordGenerator
        )
    }

    if (storeDialogVisible) {
        StoreDialog(
            onConfirm = { storageType, storeIsPrivate ->
                storeDialogVisible = false

                scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                coroutineScope.launch(Dispatchers.IO) {
                    loadingDialogVisible = true

                    val fileName = viewModel.store(storageType, storeIsPrivate)

                    loadingDialogVisible = false

                    scaffoldListState.popupHostState.currentPopupData?.dismiss()
                    scaffoldListState.popupHostState.showPopup(message = intl.translate(
                        id = "popup.store.message",
                        value = "fileName" to fileName
                    ))
                }
            },
            onCancel = { storeDialogVisible = false },
            isSingle = true
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
                        scaffoldListState.popupHostState.showPopup(message = intl.translate(
                            id = "popup.unstore.message",
                            value = "fileName" to filePath.fileName
                        ))
                    }.onFailure { error ->
                        loadingDialogVisible = false

                        scaffoldListState.popupHostState.currentPopupData?.dismiss()

                        scaffoldListState.popupHostState.showPopup(message = when (error) {
                            is ChainLinkListViewModel.StorableFormatError -> {
                                intl.translate("popup.unstore.storable.format.error")
                            }
                            is ChainLinkListViewModel.StorableMultipleError -> {
                                intl.translate("popup.unstore.storable.multiple.error")
                            }
                            is Chain.KeyInvalidError -> intl.translate("popup.chain.key.error")
                            else -> "Error"
                        })
                    }
                }
            },
            onCancel = { unstoreDialogVisible = false }
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