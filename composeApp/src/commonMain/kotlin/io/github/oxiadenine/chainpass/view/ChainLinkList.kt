package io.github.oxiadenine.chainpass.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.github.oxiadenine.chainpass.*
import io.github.oxiadenine.chainpass.security.PasswordGenerator
import io.github.oxiadenine.chainpass.component.*
import io.github.oxiadenine.chainpass.network.SyncClient
import io.github.oxiadenine.composeapp.generated.resources.*
import io.github.oxiadenine.composeapp.generated.resources.Res
import io.github.oxiadenine.composeapp.generated.resources.popup_sync_device_message
import io.github.oxiadenine.composeapp.generated.resources.popup_sync_network_error
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

data class ChainLinkListItemMenuState(
    val expanded: Boolean = false,
    val offset: IntOffset = IntOffset.Zero,
    val size: DpSize = DpSize(150.dp, 160.dp)
)

sealed class ChainLinkListEvent {
    data object Sync : ChainLinkListEvent()
    data class Store(val fileName: String) : ChainLinkListEvent()
    data class Unstore(val fileName: String) : ChainLinkListEvent()
    data class ItemCopyPassword(val password: ChainLink.Password) : ChainLinkListEvent()
    data object ItemStartEdit : ChainLinkListEvent()
    data object ItemEdit : ChainLinkListEvent()
    data object ItemCancelEdit : ChainLinkListEvent()
    data class ItemRemoveLater(val chainLink: ChainLink) : ChainLinkListEvent()
    data object ItemRemove : ChainLinkListEvent()
    data object ItemUndoRemove : ChainLinkListEvent()
}

data class ChainLinkListState(
    val isLoading: Boolean = false,
    val isSearch: Boolean = false,
    val chain: Chain? = null,
    val chainLinks: List<ChainLink> = emptyList(),
    val chainLinkSelected: ChainLink? = null,
    val chainLinksSearch: List<ChainLink> = emptyList(),
    val chainLinkEdit: ChainLink? = null,
    val chainLinksRemoved: List<ChainLink> = emptyList(),
    val event: ChainLinkListEvent? = null,
    val error: Throwable? = null
) {
    val chainLinkSelectedIndex = chainLinks.indexOfFirst { chainLink ->
        chainLink.id == chainLinkSelected?.id
    }
}

@Composable
fun ChainLinkList(
    chainId: String,
    chainKey: Chain.Key,
    viewModel: ChainLinkListViewModel,
    onTopAppBarBackClick: () -> Unit,
    deviceAddress: String,
    passwordGenerator: PasswordGenerator
) {
    val clipboardManager = LocalClipboardManager.current

    val coroutineScope = rememberCoroutineScope()

    val scaffoldListState = rememberScaffoldListState()

    var itemNewDialogVisible by rememberSaveable { mutableStateOf(false) }
    var itemEditDialogVisible by rememberSaveable { mutableStateOf(false) }

    var storeDialogVisible by rememberSaveable { mutableStateOf(false) }
    var unstoreDialogVisible by rememberSaveable { mutableStateOf(false) }

    var itemMenuState by remember { mutableStateOf(ChainLinkListItemMenuState()) }

    val dataState by viewModel.dataStateFlow.collectAsState()

    when (val event = dataState.event) {
        is ChainLinkListEvent.Sync -> {
            viewModel.getAll(chainId, chainKey)

            viewModel.clearEvent()
        }
        is ChainLinkListEvent.Store -> {
            scaffoldListState.popupHostState.currentPopupData?.dismiss()

            coroutineScope.launch {
                scaffoldListState.popupHostState.showPopup(
                    message = getString(Res.string.popup_store_message, event.fileName)
                )
            }

            viewModel.clearEvent()
        }
        is ChainLinkListEvent.Unstore -> {
            viewModel.getAll(chainId, chainKey)

            scaffoldListState.popupHostState.currentPopupData?.dismiss()

            coroutineScope.launch {
                scaffoldListState.popupHostState.showPopup(
                    message = getString(Res.string.popup_unstore_message, event.fileName)
                )
            }

            viewModel.clearEvent()
        }
        is ChainLinkListEvent.ItemCopyPassword -> {
            scaffoldListState.popupHostState.currentPopupData?.dismiss()

            clipboardManager.setText(AnnotatedString(event.password.value))

            coroutineScope.launch {
                scaffoldListState.popupHostState.showPopup(
                    message = getString(Res.string.popup_chainLink_password_message)
                )
            }

            viewModel.clearEvent()
        }
        is ChainLinkListEvent.ItemRemoveLater -> {
            coroutineScope.launch {
                when (scaffoldListState.snackbarHostState.showSnackbar(
                    message = getString(Res.string.snackbar_label_delete_text, event.chainLink.name.value),
                    actionLabel = getString(Res.string.snackbar_button_undo_text),
                    duration = SnackbarDuration.Short
                )) {
                    SnackbarResult.ActionPerformed -> viewModel.undoRemove()
                    SnackbarResult.Dismissed -> viewModel.remove()
                }
            }

            viewModel.clearEvent()
        }
        else -> viewModel.clearEvent()
    }

    dataState.error?.let { error ->
        scaffoldListState.popupHostState.currentPopupData?.dismiss()

        coroutineScope.launch {
            scaffoldListState.popupHostState.showPopup(
                message = when (error) {
                    is SyncClient.SyncNetworkError -> getString(Res.string.popup_sync_network_error)
                    is Storage.StorableFormatError -> getString(Res.string.popup_unstore_storable_format_error)
                    is Storage.StorablePrivateError -> getString(Res.string.popup_unstore_storable_private_error)
                    is Storage.StorableMultipleError -> getString(Res.string.popup_unstore_storable_multiple_error)
                    is Chain.KeyInvalidError -> getString(Res.string.popup_chain_key_error)
                    else -> "Error"
                }
            )
        }

        viewModel.clearError()
    }

    BackHandler {
        if (dataState.isSearch) {
            viewModel.cancelSearch()
        } else onTopAppBarBackClick()
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
                                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Undo,
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
            if (dataState.isSearch) {
                ChainLinkSearchListTopAppBar(
                    onBackClick = {
                        coroutineScope.launch {
                            scaffoldListState.lazyListState.animateScrollToItem(0)
                        }

                        viewModel.cancelSearch()
                    },
                    onKeywordChange = { keyword -> viewModel.search(keyword) }
                )
            } else {
                ChainLinkListTopAppBar(
                    onBackClick = {
                        scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                        onTopAppBarBackClick()
                    },
                    onSearchClick = {
                        scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()
                        scaffoldListState.popupHostState.currentPopupData?.dismiss()

                        coroutineScope.launch {
                            scaffoldListState.lazyListState.animateScrollToItem(0)
                        }

                        viewModel.startSearch()
                    },
                    onMenuItemClick = { menuItem ->
                        when (menuItem) {
                            ChainLinkListTopAppBarMenuItem.SYNC -> {
                                scaffoldListState.snackbarHostState.currentSnackbarData?.performAction()

                                if (deviceAddress.isEmpty()) {
                                    scaffoldListState.popupHostState.currentPopupData?.dismiss()

                                    coroutineScope.launch {
                                        scaffoldListState.popupHostState.showPopup(
                                            message = getString(Res.string.popup_sync_device_message)
                                        )
                                    }
                                } else viewModel.sync(deviceAddress)
                            }
                            ChainLinkListTopAppBarMenuItem.STORE -> storeDialogVisible = true
                            ChainLinkListTopAppBarMenuItem.UNSTORE -> unstoreDialogVisible = true
                        }
                    },
                    title = dataState.chain?.name?.value ?: ""
                )
            }
        },
        floatingActionButton = {
            if (!dataState.isSearch) {
                FloatingActionButton(
                    onClick = { itemNewDialogVisible = true },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                ) { Icon(imageVector = Icons.Default.Add, contentDescription = null) }
            }
        }
    ) { lazyListState ->
        val screen = LocalScreen.current

        Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val pointerInputChange = awaitPointerEvent().changes.first()

                    if (pointerInputChange.pressed &&
                        (pointerInputChange.type == PointerType.Mouse ||
                                pointerInputChange.type == PointerType.Touch)
                    ) {
                        val topAppBarHeight = screen.height.toPx() - size.height

                        val positionX = pointerInputChange.position.x
                        val positionY = pointerInputChange.position.y + topAppBarHeight

                        itemMenuState = itemMenuState.copy(
                            offset = IntOffset(
                                x = if (positionX + itemMenuState.size.width.toPx() > size.width) {
                                    (positionX - itemMenuState.size.width.toPx()).toInt()
                                } else positionX.toInt(),
                                y = if (positionY + itemMenuState.size.height.toPx() > size.height) {
                                    (positionY - itemMenuState.size.height.toPx()).toInt()
                                } else positionY.toInt()
                            )
                        )
                    }
                }
            }
        }) {
            if (dataState.isSearch) {
                val chainLinks = dataState.chainLinksSearch.toTypedArray()

                if (chainLinks.isEmpty()) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) { Text(text = stringResource(Res.string.list_chainLink_search_empty_text)) }
                } else {
                    LazyColumn(state = lazyListState, modifier = Modifier.fillMaxWidth()) {
                        items(items = chainLinks, key = { chainLink -> chainLink.id }) { chainLink ->
                            ChainLinkSearchListItem(
                                onClick = { viewModel.cancelSearch(chainLink) },
                                name = chainLink.name.value,
                                description = chainLink.description.value
                            )
                        }
                    }
                }
            } else {
                val chainLinks = dataState.chainLinks.toTypedArray()

                if (chainLinks.isEmpty()) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(Res.string.list_chainLink_empty_text))
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                } else {
                    LazyColumn(state = lazyListState, modifier = Modifier.fillMaxWidth()) {
                        items(items = chainLinks, key = { chainLink -> chainLink.id }) {chainLink ->
                            ChainLinkListItem(
                                onClick = {
                                    viewModel.select(chainLink)

                                    itemMenuState = itemMenuState.copy(expanded = true)
                                },
                                name = chainLink.name.value,
                                description = chainLink.description.value
                            )
                        }
                    }
                }
            }

            dataState.chainLinkSelectedIndex.takeIf { index -> index != -1 }?.let { index ->
                LaunchedEffect(index) {
                    lazyListState.animateScrollToItem(index)
                }
            }
        }
    }

    if (itemMenuState.expanded) {
        Popup(
            onDismissRequest = { itemMenuState = itemMenuState.copy(expanded = false) },
            offset = itemMenuState.offset,
            properties = PopupProperties(focusable = true)
        ) {
            Surface(
                modifier = Modifier.size(itemMenuState.size),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(top = 8.dp, bottom = 8.dp)) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.list_chainLink_menu_item_copy_text),
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            viewModel.copyPassword()

                            itemMenuState = itemMenuState.copy(expanded = false)
                        },
                        modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CopyAll, contentDescription = null)
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.list_chainLink_menu_item_edit_text),
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            itemMenuState = itemMenuState.copy(expanded = false)

                            scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                            viewModel.startEdit()

                            itemEditDialogVisible = true
                        },
                        modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.list_chainLink_menu_item_delete_text),
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            itemMenuState = itemMenuState.copy(expanded = false)

                            scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                            viewModel.removeLater()
                        },
                        modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    if (itemNewDialogVisible) {
        ChainLinkListItemNewDialog(
            onConfirm = { chainLinkName, chainLinkDescription, chainLinkPassword ->
                itemNewDialogVisible = false

                viewModel.new(chainLinkName, chainLinkDescription, chainLinkPassword)
            },
            onCancel = { itemNewDialogVisible = false },
            passwordGenerator = passwordGenerator
        )
    }

    if (itemEditDialogVisible) {
        ChainLinkListItemEditDialog(
            onConfirm = { chainLinkDescription, chainLinkPassword ->
                itemEditDialogVisible = false

                viewModel.edit(chainLinkDescription, chainLinkPassword)
            },
            onCancel = {
                itemEditDialogVisible = false

                viewModel.cancelEdit()
            },
            chainLink = dataState.chainLinkEdit!!,
            passwordGenerator = passwordGenerator
        )
    }

    if (storeDialogVisible) {
        StoreDialog(
            onConfirm = { storageType, storeIsPrivate ->
                storeDialogVisible = false

                scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                viewModel.store(storageType, storeIsPrivate)
            },
            onCancel = { storeDialogVisible = false },
            isSingle = true
        )
    }

    if (unstoreDialogVisible) {
        UnstoreDialog(
            onConfirm = { fileSelected ->
                unstoreDialogVisible = false

                scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                viewModel.unstore(fileSelected)
            },
            onCancel = { unstoreDialogVisible = false }
        )
    }

    if (dataState.isLoading) {
        LoadingDialog()
    }

    LaunchedEffect(viewModel) {
        viewModel.getAll(chainId, chainKey)
    }
}