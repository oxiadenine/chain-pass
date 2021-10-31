package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.component.InputDialog
import io.sunland.chainpass.common.component.VerticalScrollbar
import kotlinx.coroutines.*

enum class ChainListItemStatus { ACTUAL, DRAFT }

data class ChainListItem(var id: Int, var name: String, var key: String, var status: ChainListItemStatus)

@Composable
fun ChainList(
    coroutineScope: CoroutineScope,
    viewModel: ChainListViewModel,
    onItemSelect: (ChainListItem) -> Unit,
    onItemDelete: (ChainListItem) -> Unit
) {
    if (viewModel.chainListItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                Text(text = "New Chain")
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberScrollState()

            Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                viewModel.chainListItems.forEach { chainListItem ->
                    val keyInputDialogVisible = remember { mutableStateOf(false) }

                    if (keyInputDialogVisible.value) {
                        val keyInputState = remember { mutableStateOf("") }
                        val keyInputErrorState = remember { mutableStateOf(false) }

                        InputDialog(
                            title = null,
                            placeholder = "Key",
                            value = keyInputState.value,
                            ontValueChange = {
                                keyInputState.value = it
                                keyInputErrorState.value = keyInputState.value.isEmpty()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            isError = keyInputErrorState.value,
                            onDismissRequest = { keyInputDialogVisible.value = false },
                            onConfirmRequest = {
                                keyInputErrorState.value = keyInputState.value != chainListItem.key

                                if (!keyInputErrorState.value) {
                                    keyInputDialogVisible.value = false

                                    onItemSelect(chainListItem)
                                }
                            }
                        )
                    }

                    when (chainListItem.status) {
                        ChainListItemStatus.ACTUAL -> ChainListItem(
                            name = chainListItem.name,
                            key = chainListItem.key,
                            onClick = { keyInputDialogVisible.value = true },
                            onIconDeleteClick = {
                                viewModel.chainListItems.remove(chainListItem)

                                onItemDelete(chainListItem)
                            }
                        )
                        ChainListItemStatus.DRAFT -> ChainListItemDraft(
                            chainListItem = chainListItem,
                            onIconDoneClick = {
                                chainListItem.status = ChainListItemStatus.ACTUAL

                                coroutineScope.launch {
                                    viewModel.create(chainListItem)
                                    viewModel.refresh()
                                }
                            },
                            onIconClearClick = { viewModel.chainListItems.remove(chainListItem) }
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
