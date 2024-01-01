package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.ChainLink
import io.sunland.chainpass.common.ChainLinkStatus
import io.sunland.chainpass.common.component.DropdownMenu
import io.sunland.chainpass.common.component.DropdownMenuItem
import io.sunland.chainpass.common.component.VerticalScrollbar

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChainLinkList(
    viewModel: ChainLinkListViewModel,
    onItemNew: (ChainLink) -> Unit,
    onItemEdit: (ChainLink) -> Unit,
    onItemRemove: (Chain, ChainLink) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        val actionMenuExpandedState = remember { mutableStateOf(false) }

        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = { Text("Chain Links") },
            navigationIcon = {
                IconButton(
                    modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    onClick = onBack
                ) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null) }
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
                }
            }
        )
        Box(modifier = Modifier.fillMaxSize()) {
            if (viewModel.chainLinks.isEmpty()) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    Text(text = "New Chain Link")
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            } else {
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
                                    onIconClearClick = { viewModel.endEdit(chainLink) }
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
}
