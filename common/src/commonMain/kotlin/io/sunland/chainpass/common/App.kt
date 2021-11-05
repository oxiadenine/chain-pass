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
                coroutineScope.launch {
                    chainListViewModel.read().onFailure { exception ->
                        scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                    }
                }

                ChainList(
                    viewModel = chainListViewModel,
                    onItemCreate = { chainListItem ->
                        coroutineScope.launch {
                            chainListViewModel.create(chainListItem).fold(
                                onSuccess = {
                                    chainListItem.status = ChainListItemStatus.ACTUAL

                                    chainListViewModel.refresh()
                                },
                                onFailure = { exception ->
                                    scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                                }
                            )
                        }
                    },
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
                                SnackbarResult.Dismissed -> {
                                    chainListViewModel.delete(chainListItem).onFailure { exception ->
                                        chainListViewModel.chainListItems.add(chainListItem)

                                        scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                                    }
                                }
                            }
                        }
                    }
                )
            }
            Screen.CHAIN_LINK_LIST -> {
                coroutineScope.launch {
                    chainLinkListViewModel.read().onFailure { exception ->
                        scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                    }
                }

                ChainLinkList(
                    viewModel = chainLinkListViewModel,
                    onItemCreate = { chainLinkListItem ->
                        coroutineScope.launch {
                            chainLinkListViewModel.create(chainLinkListItem).fold(
                                onSuccess = {
                                    chainLinkListItem.status = ChainLinkListItemStatus.ACTUAL

                                    chainLinkListViewModel.refresh()
                                },
                                onFailure = { exception ->
                                    scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                                }
                            )
                        }
                    },
                    onItemUpdate = { chainLinkListItem ->
                        coroutineScope.launch {
                            chainLinkListViewModel.update(chainLinkListItem).fold(
                                onSuccess = {
                                    chainLinkListItem.status = ChainLinkListItemStatus.ACTUAL

                                    chainLinkListViewModel.refresh()
                                },
                                onFailure = { exception ->
                                    scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                                }
                            )
                        }
                    },
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
                                SnackbarResult.Dismissed -> {
                                    chainLinkListViewModel.delete(chainLinkListItem).onFailure { exception ->
                                        chainLinkListViewModel.chainLinkListItems.add(chainLinkListItem)

                                        scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
