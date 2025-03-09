package io.github.oxiadenine.chainpass.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.oxiadenine.chainpass.ChainLink
import io.github.oxiadenine.chainpass.component.LoadingDialog
import io.github.oxiadenine.common.generated.resources.Res
import io.github.oxiadenine.common.generated.resources.list_chainLink_search_empty_text
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChainLinkSearchList(
    chainId: String,
    viewModel: ChainLinkSearchListViewModel,
    onTopAppBarBackClick: () -> Unit,
    onListItemClick: (ChainLink) -> Unit
) {
    var loadingDialogVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ChainLinkSearchListTopAppBar(
            onBackClick = { onTopAppBarBackClick() },
            onKeywordChange = { keyword -> viewModel.search(keyword) }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            val chainLinks = viewModel.chainLinkSearchListState.toTypedArray()

            if (chainLinks.isEmpty()) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) { Text(text = stringResource(Res.string.list_chainLink_search_empty_text)) }
            } else {
                val lazyListState = rememberLazyListState()

                LazyColumn(state = lazyListState, modifier = Modifier.fillMaxWidth()) {
                    items(items = chainLinks, key = { chainLink -> chainLink.id }) { chainLink ->
                        ChainLinkSearchListItem(
                            onClick = { onListItemClick(chainLink) },
                            name = chainLink.name.value,
                            description = chainLink.description.value
                        )
                    }
                }
            }
        }
    }

    if (loadingDialogVisible) {
        LoadingDialog()
    }

    LaunchedEffect(viewModel) {
        loadingDialogVisible = true

        viewModel.getAll(chainId)

        loadingDialogVisible = false
    }
}