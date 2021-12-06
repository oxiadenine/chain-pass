package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.ChainLinkStatus
import io.sunland.chainpass.common.component.VerticalScrollbar

@Composable
fun ChainLinkList(
    viewModel: ChainLinkListViewModel,
    onItemNew: (ChainLink) -> Unit,
    onItemEdit: (ChainLink) -> Unit,
    onItemRemove: (Chain, ChainLink) -> Unit
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
                        ChainLinkStatus.ACTUAL -> ChainLinkListItem(
                            chainLink = chainLink,
                            onIconEditClick = { viewModel.startEdit(chainLink.id) },
                            onIconDeleteClick = { viewModel.remove(chainLink, onItemRemove) }
                        )
                        ChainLinkStatus.DRAFT -> ChainLinkListItemDraft(
                            chainLink = chainLink,
                            onIconDoneClick = { onItemNew(chainLink) },
                            onIconClearClick = { viewModel.rejectDraft(chainLink) }
                        )
                        ChainLinkStatus.EDIT -> key(chainLink.id) {
                            ChainLinkListItemEdit(
                                chainLink = chainLink,
                                onIconDoneClick = { onItemEdit(chainLink) },
                                onIconClearClick = { viewModel.endEdit(chainLink)}
                            )
                        }
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
