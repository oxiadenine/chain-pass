package io.sunland.chainpass.common.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sunland.chainpass.common.Chain
import io.sunland.chainpass.common.ChainLink
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
                if (viewModel.isSearchState.value) {
                    IconButton(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = { viewModel.endSearch() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary.let { color ->
                                Color(color.red, color.green, color.blue, color.alpha / 2)
                            })
                    }
                } else {
                    IconButton(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                        onClick = onBack
                    ) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null) }
                }
            },
            actions = {
                if (viewModel.isSearchState.value) {
                    val focusRequester = FocusRequester()

                    TextField(
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        placeholder = { Text(text = "Search") },
                        value = viewModel.searchKeywordState.value,
                        onValueChange = { value -> viewModel.search(value) },
                        trailingIcon = if (viewModel.searchKeywordState.value.isNotEmpty()) {
                            { IconButton(
                                modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                                onClick = { viewModel.search("") }
                            ) { Icon(imageVector = Icons.Default.Clear, contentDescription = null) } }
                        } else null,
                        singleLine = true,
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        ),
                        keyboardActions = KeyboardActions(onDone = { viewModel.search() })
                    )

                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                } else {
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

                                viewModel.startSearch()
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null)
                                Text(text = "Search", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        )
        Box(modifier = Modifier.fillMaxSize()) {
            if (viewModel.isSearchState.value) {
                if (viewModel.searchChainLinks.isEmpty()) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                    ) {
                        Text(text = "No Chain Links")
                        Icon(imageVector = Icons.Default.List, contentDescription = null)
                    }
                } else {
                    val scrollState = rememberScrollState()

                    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                        viewModel.searchChainLinks.forEach { chainLink ->
                            ChainLinkListItem(
                                chainLink = chainLink,
                                onIconEditClick = { viewModel.startEdit(chainLink.id) },
                                onIconDeleteClick = { viewModel.remove(chainLink, onItemRemove) },
                                isSearch = viewModel.isSearchState.value
                            )
                        }
                    }
                    VerticalScrollbar(
                        modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd),
                        scrollState = scrollState
                    )
                }
            } else {
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
                                ChainLink.Status.ACTUAL -> ChainLinkListItem(
                                    chainLink = chainLink,
                                    onIconEditClick = { viewModel.startEdit(chainLink.id) },
                                    onIconDeleteClick = { viewModel.remove(chainLink, onItemRemove) },
                                    isSearch = viewModel.isSearchState.value
                                )
                                ChainLink.Status.DRAFT -> ChainLinkListItemDraft(
                                    chainLink = chainLink,
                                    onIconDoneClick = { onItemNew(chainLink) },
                                    onIconClearClick = { viewModel.rejectDraft(chainLink) }
                                )
                                ChainLink.Status.EDIT -> key(chainLink.id) {
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
}
