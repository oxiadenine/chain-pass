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

enum class ChainListItemStatus { ACTUAL, DRAFT }

data class ChainListItem(var id: Int, var name: String, var key: String, var status: ChainListItemStatus)

@Composable
fun ChainList(
    viewModel: ChainListViewModel,
    onItemNew: (ChainListItem) -> Unit,
    onItemSelect: (ChainListItem) -> Unit,
    onItemRemove: (ChainListItem) -> Unit
) {
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
                                keyInputErrorState.value = keyInputState.value.isEmpty()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            isError = keyInputErrorState.value,
                            onDismissRequest = { keyInputDialogVisible.value = false },
                            onConfirmRequest = {
                                keyInputErrorState.value = keyInputState.value != chain.key

                                if (!keyInputErrorState.value) {
                                    keyInputDialogVisible.value = false

                                    onItemSelect(chain)
                                }
                            }
                        )
                    }

                    when (chain.status) {
                        ChainListItemStatus.ACTUAL -> ChainListItem(
                            name = chain.name,
                            key = chain.key,
                            onClick = { keyInputDialogVisible.value = true },
                            onIconDeleteClick = {
                                viewModel.chains.remove(chain)

                                onItemRemove(chain)
                            }
                        )
                        ChainListItemStatus.DRAFT -> ChainListItemDraft(
                            chainListItem = chain,
                            onIconDoneClick = { onItemNew(chain) },
                            onIconClearClick = { viewModel.chains.remove(chain) }
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
