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
    onItemNew: (ChainLinkListItem) -> Unit,
    onItemEdit: (ChainLinkListItem) -> Unit,
    onItemRemove: (ChainLinkListItem) -> Unit
) {
    if (viewModel.chainLinks.isEmpty()) {
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
                viewModel.chainLinks.forEach { chainLink ->
                    when (chainLink.status) {
                        ChainLinkListItemStatus.ACTUAL -> ChainLinkListItem(
                            name = chainLink.name,
                            password = chainLink.password,
                            onIconEditClick = {
                                chainLink.status = ChainLinkListItemStatus.EDIT

                                viewModel.refresh()
                            },
                            onIconDeleteClick = {
                                viewModel.chainLinks.remove(chainLink)

                                onItemRemove(chainLink)
                            }
                        )
                        ChainLinkListItemStatus.DRAFT -> ChainLinkListItemDraft(
                            chainLinkListItem = chainLink,
                            onIconDoneClick = { onItemNew(chainLink) },
                            onIconClearClick = { viewModel.chainLinks.remove(chainLink) }
                        )
                        ChainLinkListItemStatus.EDIT -> ChainLinkListItemEdit(
                            password = chainLink.password,
                            chainLinkListItem = chainLink,
                            onIconDoneClick = { onItemEdit(chainLink) },
                            onIconClearClick = {
                                chainLink.status = ChainLinkListItemStatus.ACTUAL

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
