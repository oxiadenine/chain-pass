package io.github.oxiadenine.chainpass.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.github.oxiadenine.chainpass.*
import io.github.oxiadenine.chainpass.component.*
import io.github.oxiadenine.chainpass.network.SyncClient
import io.github.oxiadenine.chainpass.security.PasswordGenerator
import io.github.oxiadenine.composeapp.generated.resources.*
import io.github.oxiadenine.composeapp.generated.resources.Res
import io.github.oxiadenine.composeapp.generated.resources.popup_sync_device_message
import io.github.oxiadenine.composeapp.generated.resources.popup_sync_network_error
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

enum class ChainListItemMenuItem { OPEN, DELETE, NONE }

data class ChainListItemMenuState(
    val expanded: Boolean = false,
    val offset: IntOffset = IntOffset.Zero,
    val size: DpSize = DpSize(150.dp, 112.dp),
    val itemSelected: ChainListItemMenuItem = ChainListItemMenuItem.NONE
) {
    companion object {
        val Saver = listSaver(
            save = { state -> listOf(state.value.itemSelected.name) },
            restore = {
                mutableStateOf(ChainListItemMenuState(
                    itemSelected = ChainListItemMenuItem.valueOf(it[0]))
                )
            }
        )
    }
}

sealed class ChainListEvent {
    data object Sync : ChainListEvent()
    data class Store(val fileName: String) : ChainListEvent()
    data class Unstore(val fileName: String) : ChainListEvent()
    data object ItemNew : ChainListEvent()
    data class ItemOpen(val chain: Chain) : ChainListEvent()
    data class ItemRemoveLater(val chain: Chain) : ChainListEvent()
    data object ItemRemove : ChainListEvent()
    data object ItemUndoRemove : ChainListEvent()
}

data class ChainListState(
    val isFirstLoad: Boolean = false,
    val isLoading: Boolean = false,
    val chains: List<Chain> = emptyList(),
    val chainSelected: Chain? = null,
    val chainsRemoved: List<Chain> = emptyList(),
    val event: ChainListEvent? = null,
    val error: Throwable? = null
) {
    val chainSelectedIndex = chains.indexOfFirst { chain ->
        chain.id == chainSelected?.id
    }
}

@Composable
fun ChainList(
    viewModel: ChainListViewModel,
    onTopAppBarMenuClick: () -> Unit,
    onListItemOpenMenuItemClick: (Chain) -> Unit,
    deviceAddress: String,
    passwordGenerator: PasswordGenerator
) {
    val coroutineScope = rememberCoroutineScope()

    val scaffoldListState = rememberScaffoldListState()

    var itemNewDialogVisible by rememberSaveable { mutableStateOf(false) }
    var itemKeyDialogVisible by rememberSaveable { mutableStateOf(false) }

    var storeDialogVisible by rememberSaveable { mutableStateOf(false) }
    var unstoreDialogVisible by rememberSaveable { mutableStateOf(false) }

    var itemMenuState by rememberSaveable(saver = ChainListItemMenuState.Saver) {
        mutableStateOf(ChainListItemMenuState())
    }

    val dataState by viewModel.dataStateFlow.collectAsState()

    when (val event = dataState.event) {
        is ChainListEvent.Sync -> {
            viewModel.getAll()

            viewModel.clearEvent()
        }
        is ChainListEvent.Store -> {
            scaffoldListState.popupHostState.currentPopupData?.dismiss()

            coroutineScope.launch {
                scaffoldListState.popupHostState.showPopup(
                    message = getString(Res.string.popup_store_message, event.fileName)
                )
            }

            viewModel.clearEvent()
        }
        is ChainListEvent.Unstore -> {
            viewModel.getAll()

            scaffoldListState.popupHostState.currentPopupData?.dismiss()

            coroutineScope.launch {
                scaffoldListState.popupHostState.showPopup(
                    message = getString(Res.string.popup_unstore_message, event.fileName)
                )
            }

            viewModel.clearEvent()
        }
        is ChainListEvent.ItemOpen -> {
            onListItemOpenMenuItemClick(event.chain)

            viewModel.clearEvent()
        }
        is ChainListEvent.ItemRemoveLater -> {
            coroutineScope.launch {
                when (scaffoldListState.snackbarHostState.showSnackbar(
                    message = getString(Res.string.snackbar_label_delete_text, event.chain.name.value),
                    actionLabel = getString(Res.string.snackbar_button_undo_text),
                    duration = SnackbarDuration.Short
                )) {
                    SnackbarResult.ActionPerformed -> viewModel.undoRemove()
                    SnackbarResult.Dismissed -> viewModel.remove()
                }
            }

            viewModel.clearEvent()
        }
        is ChainListEvent.ItemRemove -> viewModel.clearEvent()
        else -> Unit
    }

    dataState.error?.let { error ->
        if (dataState.event is ChainListEvent.ItemRemove) {
            viewModel.undoRemove()
        }

        scaffoldListState.popupHostState.currentPopupData?.dismiss()

        coroutineScope.launch {
            scaffoldListState.popupHostState.showPopup(
                message = when (error) {
                    is SyncClient.SyncNetworkError -> {
                        getString(Res.string.popup_sync_network_error)
                    }
                    is Storage.StorableFormatError -> {
                        getString(Res.string.popup_unstore_storable_format_error)
                    }
                    is Storage.StorablePrivateError -> {
                        getString(Res.string.popup_unstore_storable_private_error)
                    }
                    is Chain.KeyInvalidError -> {
                        getString(Res.string.popup_chain_key_error)
                    }
                    else -> "Error"
                }
            )
        }

        viewModel.clearError()
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
            ChainListTopAppBar(
                onMenuClick = {
                    scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                    onTopAppBarMenuClick()
                },
                onMenuItemClick = { menuItem ->
                    when (menuItem) {
                        ChainListTopAppBarMenuItem.SYNC -> {
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
                        ChainListTopAppBarMenuItem.STORE -> storeDialogVisible = true
                        ChainListTopAppBarMenuItem.UNSTORE -> unstoreDialogVisible = true
                    }
                },
                title = "Chain Pass",
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { itemNewDialogVisible = true },
                modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
            ) { Icon(imageVector = Icons.Default.Add, contentDescription = null) }
        }
    ) { lazyListState ->
        Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val pointerInputChange = awaitPointerEvent().changes.first()

                    if (pointerInputChange.pressed &&
                        (pointerInputChange.type == PointerType.Mouse ||
                                pointerInputChange.type == PointerType.Touch)
                    ) {
                        val positionX = pointerInputChange.position.x
                        val positionY = pointerInputChange.position.y

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
            if (dataState.chains.isEmpty() && !dataState.isFirstLoad) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(Res.string.list_chain_empty_text))
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            } else {
                LazyColumn(state = lazyListState, modifier = Modifier.fillMaxWidth()) {
                    items(items = dataState.chains.toTypedArray(), key = { chain -> chain.id }) { chain ->
                        ChainListItem(
                            onClick = {
                                viewModel.select(chain)

                                itemMenuState = itemMenuState.copy(expanded = true)
                            },
                            name = chain.name.value
                        )
                    }
                }

                when (dataState.event) {
                    is ChainListEvent.ItemNew, is ChainListEvent.ItemUndoRemove -> {
                        dataState.chainSelectedIndex.takeIf { index -> index != -1 }?.let { index ->
                            LaunchedEffect(index) {
                                lazyListState.animateScrollToItem(index)
                            }
                        }
                    }
                    else -> Unit
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
                                    text = stringResource(Res.string.list_chain_menu_item_open_text),
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                itemMenuState = itemMenuState.copy(
                                    expanded = false,
                                    itemSelected = ChainListItemMenuItem.OPEN
                                )

                                itemKeyDialogVisible = true
                            },
                            modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.LockOpen, contentDescription = null)
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(Res.string.list_chain_menu_item_delete_text),
                                    fontSize = 14.sp)
                            },
                            onClick = {
                                itemMenuState = itemMenuState.copy(
                                    expanded = false,
                                    itemSelected = ChainListItemMenuItem.DELETE
                                )

                                itemKeyDialogVisible = true
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
    }

    if (itemNewDialogVisible) {
        ChainListItemNewDialog(
            onConfirm = { chainName, chainKey ->
                itemNewDialogVisible = false

                viewModel.new(chainName, chainKey)
            },
            onCancel = { itemNewDialogVisible = false },
            passwordGenerator = passwordGenerator
        )
    }

    if (itemKeyDialogVisible) {
        ChainListItemKeyDialog(
            onConfirm = { chainKey ->
                when (itemMenuState.itemSelected) {
                    ChainListItemMenuItem.OPEN -> {
                        itemKeyDialogVisible = false

                        scaffoldListState.snackbarHostState.currentSnackbarData?.performAction()

                        viewModel.open(chainKey)
                    }
                    ChainListItemMenuItem.DELETE -> {
                        itemKeyDialogVisible = false

                        scaffoldListState.snackbarHostState.currentSnackbarData?.dismiss()

                        viewModel.removeLater(chainKey)
                    }
                    else -> Unit
                }

                itemMenuState = itemMenuState.copy(itemSelected = ChainListItemMenuItem.NONE)
            },
            onCancel = { itemKeyDialogVisible = false }
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
            isSingle = false
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
        viewModel.getAll()
    }
}