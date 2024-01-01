package io.sunland.chainpass.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.ktor.client.*
import io.sunland.chainpass.common.view.*
import kotlinx.coroutines.launch

enum class Screen { CHAIN_LIST, CHAIN_LINK_LIST }

@Composable
fun App(httpClient: HttpClient) {
    val coroutineScope = rememberCoroutineScope()

    val screenState = remember { mutableStateOf(Screen.CHAIN_LIST) }
    val scaffoldState = rememberScaffoldState()

    val chainListViewModel = ChainListViewModel(httpClient)
    val chainLinkListViewModel = ChainLinkListViewModel(httpClient)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        topBar = {
            when (screenState.value) {
                Screen.CHAIN_LIST -> ChainListTopBar(onIconAddClick = {
                    val chain = ChainListItem(0, "", "", ChainListItemStatus.DRAFT)

                    chainListViewModel.chainListItems.add(chain)
                })
                Screen.CHAIN_LINK_LIST -> CHainLinkListTopBar(
                    onIconArrowBackClick = { screenState.value = Screen.CHAIN_LIST },
                    onIconAddClick = {
                        val chainLinkListItem = ChainLinkListItem(
                            id = 0,
                            name = "",
                            password = "",
                            chainId = chainLinkListViewModel.chainListItem!!.id,
                            status = ChainLinkListItemStatus.DRAFT
                        )

                        chainLinkListViewModel.chainLinkListItems.add(chainLinkListItem)
                    }
                )
            }
        }
    ) {
        when (screenState.value) {
            Screen.CHAIN_LIST -> {
                coroutineScope.launch { chainListViewModel.read() }

                ChainList(
                    coroutineScope = coroutineScope,
                    viewModel = chainListViewModel,
                    onItemSelect = { chainListItem ->
                        chainLinkListViewModel.chainListItem = chainListItem

                        screenState.value = Screen.CHAIN_LINK_LIST
                    },
                    onItemDelete = { chainListItem ->
                        coroutineScope.launch {
                            when (scaffoldState.snackbarHostState.showSnackbar(
                                message = "${chainListItem.name} removed",
                                actionLabel = "Dismiss",
                                duration = SnackbarDuration.Short
                            )) {
                                SnackbarResult.ActionPerformed -> {
                                    chainListViewModel.chainListItems.add(chainListItem)
                                }
                                SnackbarResult.Dismissed -> chainListViewModel.delete(chainListItem)
                            }
                        }
                    }
                )
            }
            Screen.CHAIN_LINK_LIST -> {
                coroutineScope.launch { chainLinkListViewModel.read() }

                ChainLinkList(
                    coroutineScope = coroutineScope,
                    viewModel = chainLinkListViewModel,
                    onItemDelete = { chainLinkListItem ->
                        coroutineScope.launch {
                            when (scaffoldState.snackbarHostState.showSnackbar(
                                message = "${chainLinkListItem.name} removed",
                                actionLabel = "Dismiss",
                                duration = SnackbarDuration.Short
                            )) {
                                SnackbarResult.ActionPerformed -> {
                                    chainLinkListViewModel.chainLinkListItems.add(chainLinkListItem)
                                }
                                SnackbarResult.Dismissed -> chainLinkListViewModel.delete(chainLinkListItem)
                            }
                        }
                    }
                )
            }
        }
    }
}
