package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.component.DropdownMenu
import io.sunland.chainpass.common.component.DropdownMenuItem
import io.sunland.chainpass.common.component.InputDialog
import io.sunland.chainpass.common.component.VerticalScrollbar

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainList(
    serverAddress: ServerAddress,
    viewModel: ChainListViewModel,
    onItemNew: (Chain) -> Unit,
    onItemSelect: (Chain) -> Unit,
    onItemRemove: (Chain) -> Unit,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        val actionMenuExpandedState = remember { mutableStateOf(false) }

        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Chains")
                    Text(text = "(${serverAddress.host.value}:${serverAddress.port.value})", fontSize = 12.sp)
                }
            },
            actions = {
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = { actionMenuExpandedState.value = true }
                ) { Icon(imageVector = Icons.Default.MoreVert, contentDescription = null) }
                DropdownMenu(
                    modifier = Modifier.width(width = 150.dp),
                    expanded = actionMenuExpandedState.value,
                    onDismissRequest = { actionMenuExpandedState.value = false },
                    offset = DpOffset(x = 4.dp, y = (-48).dp)
                ) {
                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = {
                            actionMenuExpandedState.value = false

                            onRefresh()
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Text(text = "Refresh", fontSize = 12.sp)
                        }
                    }
                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = {
                            actionMenuExpandedState.value = false

                            viewModel.draft()
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Text(text = "Add", fontSize = 12.sp)
                        }
                    }
                    DropdownMenuItem(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = {
                            actionMenuExpandedState.value = false

                            onDisconnect()
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                            Text(text = "Disconnect", fontSize = 12.sp)
                        }
                    }
                }
            }
        )
        Box(modifier = Modifier.fillMaxSize()) {
            if (viewModel.chains.isEmpty()) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    Text(text = "New Chain")
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            } else {
                val scrollState = rememberScrollState()

                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    viewModel.chains.forEach { chain ->
                        when (chain.status) {
                            Chain.Status.ACTUAL, Chain.Status.REMOVE, Chain.Status.SELECT -> {
                                val keyInputDialogVisible = remember { mutableStateOf(false) }

                                if (keyInputDialogVisible.value) {
                                    val keyState = mutableStateOf(chain.key.value)
                                    val keyErrorState = mutableStateOf(!chain.key.isValid)

                                    InputDialog(
                                        title = null,
                                        placeholder = "Key",
                                        value = keyState.value,
                                        ontValueChange = { key ->
                                            chain.key = Chain.Key(key)

                                            keyState.value = chain.key.value
                                            keyErrorState.value = !chain.key.isValid
                                        },
                                        visualTransformation = PasswordVisualTransformation(),
                                        isError = keyErrorState.value,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        onDismissRequest = {
                                            chain.key = Chain.Key()

                                            keyInputDialogVisible.value = false
                                        },
                                        onConfirmRequest = {
                                            chain.key = Chain.Key(keyState.value)

                                            keyErrorState.value = !chain.key.isValid

                                            if (!keyErrorState.value) {
                                                when (chain.status) {
                                                    Chain.Status.REMOVE -> viewModel.remove(chain, onItemRemove)
                                                    Chain.Status.SELECT -> viewModel.select(chain, onItemSelect)
                                                    else -> keyInputDialogVisible.value = false
                                                }

                                                keyInputDialogVisible.value = false
                                            }
                                        }
                                    )
                                }

                                ChainListItem(
                                    chain = chain,
                                    onClick = {
                                        chain.status = Chain.Status.SELECT

                                        keyInputDialogVisible.value = true
                                    },
                                    onIconDeleteClick = {
                                        chain.status = Chain.Status.REMOVE

                                        keyInputDialogVisible.value = true
                                    }
                                )
                            }
                            Chain.Status.DRAFT -> ChainListItemDraft(
                                chain = chain,
                                onIconDoneClick = { onItemNew(chain) },
                                onIconClearClick = { viewModel.rejectDraft(chain) }
                            )
                        }
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd),
                    scrollState = scrollState
                )
            }
        }
    }
}
