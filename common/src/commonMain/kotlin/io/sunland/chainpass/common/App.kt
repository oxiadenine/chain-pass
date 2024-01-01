package io.sunland.chainpass.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import io.ktor.client.*
import io.sunland.chainpass.common.view.*
import kotlinx.coroutines.launch

enum class Screen { CHAIN_LIST, CHAIN_LINK_LIST }

enum class ThemeColor(val color: Color) {
    ANTHRACITE(Color(0.22f, 0.24f, 0.26f)),
    QUARTZ(Color(0.91f, 0.87f, 0.88f)),
    COPPER(Color(0.72f, 0.46f, 0.28f))
}

@Composable
fun App(httpClient: HttpClient) = MaterialTheme(
    colors = if (isSystemInDarkTheme()) {
        darkColors(
            surface = ThemeColor.ANTHRACITE.color,
            onSurface = ThemeColor.QUARTZ.color,
            background = ThemeColor.ANTHRACITE.color,
            onBackground = ThemeColor.QUARTZ.color,
            error = ThemeColor.COPPER.color
        )
    } else lightColors(
        primary = ThemeColor.ANTHRACITE.color,
        onPrimary = ThemeColor.QUARTZ.color,
        background = ThemeColor.QUARTZ.color,
        onBackground = ThemeColor.ANTHRACITE.color,
        surface = ThemeColor.QUARTZ.color,
        onSurface = ThemeColor.ANTHRACITE.color,
        error = ThemeColor.COPPER.color
    ),
    typography = Typography(defaultFontFamily = FontFamily.Monospace),
    shapes = Shapes(
        small = RoundedCornerShape(percent = 0),
        medium = RoundedCornerShape(percent = 0),
        large = RoundedCornerShape( percent = 0)
    )
) {
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
