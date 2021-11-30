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
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.ChainStatus
import io.sunland.chainpass.common.component.InputDialog
import io.sunland.chainpass.common.component.VerticalScrollbar

@Composable
fun ChainList(viewModel: ChainListViewModel, onItemNew: (Chain) -> Unit, onItemSelect: (Chain) -> Unit, onItemRemove: (Chain) -> Unit) {
    if (viewModel.chains.isEmpty()) {
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
                viewModel.chains.forEach { chain ->
                    val keyInputDialogVisible = remember { mutableStateOf(false) }

                    if (keyInputDialogVisible.value) {
                        val keyInputState = remember { mutableStateOf("") }
                        val keyInputErrorState = remember { mutableStateOf(false) }

                        InputDialog(
                            title = null,
                            placeholder = "Key",
                            value = keyInputState.value,
                            ontValueChange = { key ->
                                keyInputState.value = key
                                keyInputErrorState.value = false
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            isError = keyInputErrorState.value,
                            onDismissRequest = { keyInputDialogVisible.value = false },
                            onConfirmRequest = {
                                runCatching {
                                    when (chain.status) {
                                        ChainStatus.REMOVE -> viewModel.remove(chain, Chain.Key(keyInputState.value))
                                        ChainStatus.SELECT -> viewModel.select(chain, Chain.Key(keyInputState.value))
                                        else -> throw IllegalStateException()
                                    }
                                }.fold(
                                    onSuccess = { chain ->
                                        keyInputErrorState.value = false
                                        keyInputDialogVisible.value = false

                                        when (chain.status) {
                                            ChainStatus.REMOVE -> onItemRemove(chain)
                                            ChainStatus.SELECT -> onItemSelect(chain)
                                            else -> Unit
                                        }
                                    },
                                    onFailure = { keyInputErrorState.value = true }
                                )
                            }
                        )
                    }

                    when (chain.status) {
                        ChainStatus.ACTUAL, ChainStatus.REMOVE, ChainStatus.SELECT -> ChainListItem(
                            chain = chain,
                            onClick = {
                                chain.status = ChainStatus.SELECT

                                keyInputDialogVisible.value = true
                            },
                            onIconDeleteClick = {
                                chain.status = ChainStatus.REMOVE

                                keyInputDialogVisible.value = true
                            }
                        )
                        ChainStatus.DRAFT -> ChainListItemDraft(
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
