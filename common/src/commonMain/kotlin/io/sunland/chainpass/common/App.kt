package io.sunland.chainpass.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalComposeUiApi::class)
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
    val chainLinkListViewModel = ChainLinkListViewModel(ChainNetRepository(httpClient), ChainLinkNetRepository(httpClient))

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            when (screenState.value) {
                Screen.CHAIN_LIST -> ChainListTopBar(
                    onIconAddClick = { chainListViewModel.draft() },
                    onIconRefreshClick = {
                        coroutineScope.launch {
                            chainListViewModel.getAll().onFailure { exception ->
                                scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                            }
                        }
                    }
                )
                Screen.CHAIN_LINK_LIST -> ChainLinkListTopBar(
                    onIconArrowBackClick = {
                        scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()

                        screenState.value = Screen.CHAIN_LIST
                    },
                    onIconAddClick = { chainLinkListViewModel.draft() },
                    onIconRefreshClick = {
                        coroutineScope.launch {
                            chainLinkListViewModel.getAll()
                                .onSuccess { screenState.value = Screen.CHAIN_LINK_LIST }
                                .onFailure { exception ->
                                    scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                                }
                        }
                    }
                )
            }
        },
        snackbarHost = { snackbarHostState ->
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        modifier = Modifier.padding(all = 16.dp),
                        content = { Text(text = snackbarData.message) },
                        action = {
                            snackbarData.actionLabel?.let { label ->
                                TextButton(
                                    modifier = Modifier.pointerHoverIcon(PointerIconDefaults.Hand),
                                    onClick = { snackbarHostState.currentSnackbarData?.performAction() }
                                ) { Text(text = label, color = ThemeColor.COPPER.color) }
                            }
                        },
                        backgroundColor = ThemeColor.ANTHRACITE.color,
                        contentColor = ThemeColor.QUARTZ.color
                    )
                }
            )
        }
    ) {
        Box {
            when (screenState.value) {
                Screen.CHAIN_LIST -> {
                    coroutineScope.launch {
                        chainListViewModel.getAll().onFailure { exception ->
                            scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                        }

                        chainLinkListViewModel.chain = null
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
                            coroutineScope.launch {
                                chainLinkListViewModel.chain = chain

                                chainLinkListViewModel.getAll()
                                    .onSuccess { screenState.value = Screen.CHAIN_LINK_LIST }
                                    .onFailure { exception ->
                                        chainLinkListViewModel.chain = null

                                        scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                                    }
                            }
                        },
                        onItemRemove = { chain ->
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()

                                when (scaffoldState.snackbarHostState.showSnackbar(
                                    message = "${chain.name.value} removed",
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
                        onItemRemove = { chain, chainLink ->
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()

                                when (scaffoldState.snackbarHostState.showSnackbar(
                                    message = "${chainLink.name.value} removed",
                                    actionLabel = "Dismiss",
                                    duration = SnackbarDuration.Short
                                )) {
                                    SnackbarResult.ActionPerformed -> chainLinkListViewModel.undoRemove(chainLink)
                                    SnackbarResult.Dismissed -> {
                                        chainLinkListViewModel.remove(chain, chainLink).onFailure { exception ->
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
}
