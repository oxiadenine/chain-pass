package io.sunland.chainpass.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import io.ktor.client.plugins.*
import io.ktor.http.*
import io.sunland.chainpass.common.repository.ChainLinkNetRepository
import io.sunland.chainpass.common.repository.ChainNetRepository
import io.sunland.chainpass.common.view.*
import kotlinx.coroutines.launch

enum class Screen { SERVER_CONNECTION, CHAIN_LIST, CHAIN_LINK_LIST }

enum class ThemeColor(val color: Color) {
    ANTHRACITE(Color(0.22f, 0.24f, 0.26f)),
    QUARTZ(Color(0.91f, 0.87f, 0.88f)),
    COPPER(Color(0.72f, 0.46f, 0.28f))
}

class AppState(
    val serverAddressState: MutableState<ServerAddress>,
    val httpClientState: MutableState<HttpClient>,
    val screenState: MutableState<Screen>,
    val isServerConnected: MutableState<Boolean>
)

@Composable
fun rememberAppState(serverAddress: ServerAddress, httpClient: HttpClient, screen: Screen) = remember {
    AppState(mutableStateOf(serverAddress), mutableStateOf(httpClient), mutableStateOf(screen), mutableStateOf(false))
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App(settingsFactory: SettingsFactory, appState: AppState) = MaterialTheme(
    colors = darkColors(
        primary = ThemeColor.QUARTZ.color,
        primaryVariant = ThemeColor.QUARTZ.color,
        secondary = ThemeColor.QUARTZ.color,
        secondaryVariant = ThemeColor.QUARTZ.color,
        background = ThemeColor.ANTHRACITE.color,
        surface = ThemeColor.ANTHRACITE.color,
        error = ThemeColor.COPPER.color,
        onPrimary = ThemeColor.ANTHRACITE.color,
        onSecondary = ThemeColor.ANTHRACITE.color,
        onBackground = ThemeColor.QUARTZ.color,
        onSurface = ThemeColor.QUARTZ.color,
        onError = ThemeColor.COPPER.color
    ),
    typography = Typography(defaultFontFamily = FontFamily.Monospace),
    shapes = Shapes(
        small = RoundedCornerShape(percent = 0),
        medium = RoundedCornerShape(percent = 0),
        large = RoundedCornerShape(percent = 0)
    )
) {
    if (!appState.isServerConnected.value) {
        settingsFactory.load(appState.serverAddressState.value)?.let { settings ->
            appState.serverAddressState.value = settings as ServerAddress
            appState.httpClientState.value = appState.httpClientState.value.config {
                defaultRequest {
                    host = settings.host.value
                    port = settings.port.value.toInt()
                    url {
                        protocol = URLProtocol.WS
                    }
                }
            }
            appState.screenState.value = Screen.CHAIN_LIST
            appState.isServerConnected.value = true
        }
    }

    val scaffoldState = rememberScaffoldState()

    val coroutineScope = rememberCoroutineScope()

    val chainListViewModel = ChainListViewModel(ChainNetRepository(appState.httpClientState.value))
    val chainLinkListViewModel = ChainLinkListViewModel(
        ChainNetRepository(appState.httpClientState.value),
        ChainLinkNetRepository(appState.httpClientState.value)
    )

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
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
            when (appState.screenState.value) {
                Screen.SERVER_CONNECTION -> {
                    ServerConnection(
                        serverAddress = appState.serverAddressState.value,
                        onIconDoneClick = { serverAddress ->
                            settingsFactory.save(serverAddress).let {
                                appState.serverAddressState.value = serverAddress
                                appState.httpClientState.value = appState.httpClientState.value.config {
                                    defaultRequest {
                                        host = serverAddress.host.value
                                        port = serverAddress.port.value.toInt()
                                        url {
                                            protocol = URLProtocol.WS
                                        }
                                    }
                                }
                                appState.screenState.value = Screen.CHAIN_LIST
                                appState.isServerConnected.value = true
                            }
                        }
                    )
                }
                Screen.CHAIN_LIST -> {
                    coroutineScope.launch {
                        chainListViewModel.getAll().onFailure { exception ->
                            scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                        }

                        chainLinkListViewModel.chain = null
                    }

                    ChainList(
                        serverAddress = appState.serverAddressState.value,
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
                                    .onSuccess { appState.screenState.value = Screen.CHAIN_LINK_LIST }
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
                        },
                        onRefresh = {
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.currentSnackbarData?.performAction()

                                chainListViewModel.getAll().onFailure { exception ->
                                    scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                                }
                            }
                        },
                        onDisconnect = {
                            scaffoldState.snackbarHostState.currentSnackbarData?.performAction()

                            settingsFactory.delete(appState.serverAddressState.value).let {
                                appState.serverAddressState.value = ServerAddress()
                                appState.httpClientState.value.close()
                                appState.screenState.value = Screen.SERVER_CONNECTION
                                appState.isServerConnected.value = false
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
                        onItemRemove = { chainLink ->
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()

                                when (scaffoldState.snackbarHostState.showSnackbar(
                                    message = "${chainLink.name.value} removed",
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
                        },
                        onRefresh = {
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.currentSnackbarData?.performAction()

                                chainLinkListViewModel.getAll().onFailure { exception ->
                                    scaffoldState.snackbarHostState.showSnackbar(exception.message!!)
                                }
                            }
                        },
                        onBack = {
                            scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()

                            appState.screenState.value = Screen.CHAIN_LIST
                        }
                    )
                }
            }
        }
    }
}