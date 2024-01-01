package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.component.VerticalScrollbar

@Composable
fun ChainList(
    serverAddress: ServerAddress,
    viewModel: ChainListViewModel,
    onNew: (Chain) -> Unit,
    onSelect: (Chain) -> Unit,
    onRemove: (Chain) -> Unit,
    onSync: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ChainListTopBar(
            serverAddress = serverAddress,
            onSync = onSync,
            onAdd = { viewModel.draft() },
            onDisconnect = onDisconnect
        )
        Box(modifier = Modifier.fillMaxSize()) {
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
                val scrollState = rememberScrollState()

                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    viewModel.chainListState.forEach { chain ->
                        when (chain.status) {
                            Chain.Status.ACTUAL -> {
                                val keyInputDialogVisible = remember { mutableStateOf(false) }

                                if (keyInputDialogVisible.value) {
                                    ChainListItemKeyInput(
                                        onDismiss = {
                                            viewModel.chainRemoveState.value = null
                                            viewModel.chainSelectState.value = null

                                            keyInputDialogVisible.value = false
                                        },
                                        onConfirm = { chainKey ->
                                            viewModel.removeLater(chainKey)?.let(onRemove)
                                            viewModel.select(chainKey)?.let(onSelect)

                                            viewModel.chainRemoveState.value = null
                                            viewModel.chainSelectState.value = null

                                            keyInputDialogVisible.value = false
                                        }
                                    )
                                }

                                ChainListItem(
                                    chain = chain,
                                    onSelect = {
                                        viewModel.chainSelectState.value = chain

                                        keyInputDialogVisible.value = true
                                    },
                                    onDelete = {
                                        viewModel.chainRemoveState.value = chain

                                        keyInputDialogVisible.value = true
                                    }
                                )
                            }
                            Chain.Status.DRAFT -> ChainListItemDraft(
                                chain = chain,
                                onNew = { onNew(chain) },
                                onCancel = { viewModel.rejectDraft(chain) }
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