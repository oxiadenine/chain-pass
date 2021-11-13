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
import io.sunland.chainpass.common.repository.ChainLinkNetRepository
import io.sunland.chainpass.common.repository.ChainNetRepository
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

    val chainListViewModel = ChainListViewModel(ChainNetRepository(httpClient))
    val chainLinkListViewModel = ChainLinkListViewModel(ChainLinkNetRepository(httpClient))

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        topBar = {
            when (screenState.value) {
                Screen.CHAIN_LIST -> ChainListTopBar(onIconAddClick = { chainListViewModel.draft() })
                Screen.CHAIN_LINK_LIST -> ChainLinkListTopBar(
                    onIconArrowBackClick = { screenState.value = Screen.CHAIN_LIST },
                    onIconAddClick = { chainLinkListViewModel.draft() }
                )
            }
        }
    ) {
        when (screenState.value) {
            Screen.CHAIN_LIST -> {
                coroutineScope.launch {
                    chainListViewModel.getAll().onFailure { exception ->
                        scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                    }
                }

                ChainList(
                    viewModel = chainListViewModel,
                    onItemNew = { chain ->
                        coroutineScope.launch {
                            chainListViewModel.new(chain).onFailure { exception ->
                                scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                            }
                        }
                    },
                    onItemSelect = { chain ->
                        chainLinkListViewModel.chain = chain

                        screenState.value = Screen.CHAIN_LINK_LIST
                    },
                    onItemRemove = { chain ->
                        coroutineScope.launch {
                            when (scaffoldState.snackbarHostState.showSnackbar(
                                message = "${chain.name} removed",
                                actionLabel = "Dismiss",
                                duration = SnackbarDuration.Short
                            )) {
                                SnackbarResult.ActionPerformed -> chainListViewModel.undoRemove(chain)
                                SnackbarResult.Dismissed -> {
                                    chainListViewModel.remove(chain).onFailure { exception ->
                                        chainListViewModel.undoRemove(chain)

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
                    chainLinkListViewModel.getAll().onFailure { exception ->
                        scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                    }
                }

                ChainLinkList(
                    viewModel = chainLinkListViewModel,
                    onItemNew = { chainLink ->
                        coroutineScope.launch {
                            chainLinkListViewModel.new(chainLink).onFailure { exception ->
                                scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                            }
                        }
                    },
                    onItemEdit = { chainLink ->
                        coroutineScope.launch {
                            chainLinkListViewModel.edit(chainLink).onFailure { exception ->
                                scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                            }
                        }
                    },
                    onItemRemove = { chainLink ->
                        coroutineScope.launch {
                            when (scaffoldState.snackbarHostState.showSnackbar(
                                message = "${chainLink.name} removed",
                                actionLabel = "Dismiss",
                                duration = SnackbarDuration.Short
                            )) {
                                SnackbarResult.ActionPerformed -> chainLinkListViewModel.undoRemove(chainLink)
                                SnackbarResult.Dismissed -> {
                                    chainLinkListViewModel.remove(chainLink).onFailure { exception ->
                                        chainLinkListViewModel.undoRemove(chainLink)

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
