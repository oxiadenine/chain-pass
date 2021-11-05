package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.component.VerticalScrollbar

enum class ChainLinkListItemStatus { ACTUAL, DRAFT, EDIT }

data class ChainLinkListItem(
    var id: Int,
    var name: String,
    var password: String,
    val chainId: Int,
    var status: ChainLinkListItemStatus
)

@Composable
fun ChainLinkList(
    viewModel: ChainLinkListViewModel,
    onItemCreate: (ChainLinkListItem) -> Unit,
    onItemUpdate: (ChainLinkListItem) -> Unit,
    onItemDelete: (ChainLinkListItem) -> Unit
) {
    if (viewModel.chainLinkListItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                Text(text = "New Chain Link")
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberScrollState()

            Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                viewModel.chainLinkListItems.forEach { chainLinkListItem ->
                    when (chainLinkListItem.status) {
                        ChainLinkListItemStatus.ACTUAL -> ChainLinkListItem(
                            name = chainLinkListItem.name,
                            password = chainLinkListItem.password,
                            onIconEditClick = {
                                chainLinkListItem.status = ChainLinkListItemStatus.EDIT

                                viewModel.refresh()
                            },
                            onIconDeleteClick = {
                                viewModel.chainLinkListItems.remove(chainLinkListItem)

                                onItemDelete(chainLinkListItem)
                            }
                        )
                        ChainLinkListItemStatus.DRAFT -> ChainLinkListItemDraft(
                            chainLinkListItem = chainLinkListItem,
                            onIconDoneClick = { onItemCreate(chainLinkListItem) },
                            onIconClearClick = { viewModel.chainLinkListItems.remove(chainLinkListItem) }
                        )
                        ChainLinkListItemStatus.EDIT -> ChainLinkListItemEdit(
                            password = chainLinkListItem.password,
                            chainLinkListItem = chainLinkListItem,
                            onIconDoneClick = { onItemUpdate(chainLinkListItem) },
                            onIconClearClick = {
                                chainLinkListItem.status = ChainLinkListItemStatus.ACTUAL

                                viewModel.refresh()
                            }
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
