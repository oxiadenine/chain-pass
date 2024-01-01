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
import io.sunland.chainpass.common.network.ChainApi
import io.sunland.chainpass.common.network.ChainLinkApi
import io.sunland.chainpass.common.network.DiscoverySocket
import io.sunland.chainpass.common.network.discover
import io.sunland.chainpass.common.view.*
import kotlinx.coroutines.launch

object Theme {
    enum class Palette(val color: Color) {
        ANTHRACITE(Color(0.22f, 0.24f, 0.26f)),
        QUARTZ(Color(0.91f, 0.87f, 0.88f)),
        COPPER(Color(0.72f, 0.46f, 0.28f))
    }
}

enum class Screen { SERVER_CONNECTION, CHAIN_LIST, CHAIN_LINK_LIST }

class NavigationState(val screenState: MutableState<Screen>)

@Composable
fun rememberNavigationState(): NavigationState = remember {
    NavigationState(mutableStateOf(Screen.SERVER_CONNECTION))
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App(httpClient: HttpClient, settingsManager: SettingsManager) = MaterialTheme(
    colors = darkColors(
        primary = Theme.Palette.QUARTZ.color,
        primaryVariant = Theme.Palette.QUARTZ.color,
        secondary = Theme.Palette.QUARTZ.color,
        secondaryVariant = Theme.Palette.QUARTZ.color,
        background = Theme.Palette.ANTHRACITE.color,
        surface = Theme.Palette.ANTHRACITE.color,
        error = Theme.Palette.COPPER.color,
        onPrimary = Theme.Palette.ANTHRACITE.color,
        onSecondary = Theme.Palette.ANTHRACITE.color,
        onBackground = Theme.Palette.QUARTZ.color,
        onSurface = Theme.Palette.QUARTZ.color,
        onError = Theme.Palette.COPPER.color
    ),
    typography = Typography(defaultFontFamily = FontFamily.Monospace),
    shapes = Shapes(
        small = RoundedCornerShape(percent = 0),
        medium = RoundedCornerShape(percent = 0),
        large = RoundedCornerShape(percent = 0)
    )
) {
    val coroutineScope = rememberCoroutineScope()

    val settingsState = remember { mutableStateOf(Settings()) }
    val navigationState = rememberNavigationState()
    val scaffoldState = rememberScaffoldState()

    settingsManager.load()?.let { settings ->
        settingsState.value = settings

        navigationState.screenState.value = if (settings.isServerConnected) {
            Screen.CHAIN_LIST
        } else Screen.SERVER_CONNECTION
    }

    val chainListViewModel = ChainListViewModel(
        Storage(settingsManager.dirPath),
        ChainApi(httpClient, settingsState.value),
        ChainLinkApi(httpClient,settingsState.value)
    )

    val chainLinkListViewModel = ChainLinkListViewModel(
        ChainApi(httpClient, settingsState.value),
        ChainLinkApi(httpClient,settingsState.value)
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
                                ) { Text(text = label, color = MaterialTheme.colors.error) }
                            }
                        },
                        backgroundColor = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.primary
                    )
                }
            )
        }
    ) {
        Box {
            when (navigationState.screenState.value) {
                Screen.SERVER_CONNECTION -> {
                    val serverConnectionState = ServerConnectionState(ServerAddress())

                    ServerConnection(
                        serverConnectionState = serverConnectionState,
                        onDiscover = {
                            serverConnectionState.discoveringState.value = coroutineScope.launch {
                                val serverAddress = DiscoverySocket.discover()

                                if (serverAddress.isEmpty()) {
                                    scaffoldState.snackbarHostState.showSnackbar("Server address cannot be discovered")
                                } else {
                                    val host = ServerAddress.Host(serverAddress.substringBefore(":"))
                                    val port = ServerAddress.Port(serverAddress.substringAfter(":"))

                                    serverConnectionState.hostState.value = host.value
                                    serverConnectionState.hostValidationState.value = host.validation

                                    serverConnectionState.portState.value = port.value
                                    serverConnectionState.portValidationState.value = port.validation
                                }

                                serverConnectionState.discoveringState.value = null
                            }
                        },
                        onDiscoverCancel = {
                            serverConnectionState.discoveringState.value?.cancel()
                            serverConnectionState.discoveringState.value = null
                        },
                        onConnect = { serverAddress ->
                            settingsState.value = Settings(
                                serverAddress.host.value,
                                serverAddress.port.value.toInt(),
                                true
                            )
                            navigationState.screenState.value = Screen.CHAIN_LIST

                            settingsManager.save(settingsState.value)
                        }
                    )
                }
                Screen.CHAIN_LIST -> {
                    coroutineScope.launch {
                        chainListViewModel.getAll().onFailure { exception ->
                            scaffoldState.snackbarHostState.showSnackbar(exception.message ?: "Error")
                        }
                    }

                    ChainList(
                        serverAddress = ServerAddress().apply {
                            host = ServerAddress.Host(settingsState.value.serverHost)
                            port = ServerAddress.Port(settingsState.value.serverPort.toString())
                        },
                        viewModel = chainListViewModel,
                        onSync = {
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.currentSnackbarData?.performAction()

                                chainListViewModel.getAll().onFailure { exception ->
                                    scaffoldState.snackbarHostState.showSnackbar(exception.message ?: "Error")
                                }
                            }
                        },
                        onNew = { chain ->
                            coroutineScope.launch {
                                chainListViewModel.new(chain).onFailure { exception ->
                                    scaffoldState.snackbarHostState.showSnackbar(exception.message ?: "Error")
                                }
                            }
                        },
                        onSelect = { chain ->
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()

                                chainLinkListViewModel.chain = chain

                                chainLinkListViewModel.getAll()
                                    .onSuccess {
                                        navigationState.screenState.value = Screen.CHAIN_LINK_LIST
                                    }.onFailure { exception ->
                                        scaffoldState.snackbarHostState.showSnackbar(exception.message ?: "Error")
                                    }
                            }
                        },
                        onRemove = { chain ->
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

                                            scaffoldState.snackbarHostState.showSnackbar(exception.message ?: "Error")
                                        }
                                    }
                                }
                            }
                        },
                        onStore = { chain, storeOptions ->
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()

                                chainListViewModel.store(chain, storeOptions)
                                    .onSuccess { fileName ->
                                        scaffoldState.snackbarHostState.showSnackbar("Stored to $fileName")
                                    }.onFailure { exception ->
                                        scaffoldState.snackbarHostState.showSnackbar(exception.message ?: "Error")
                                    }
                            }
                        },
                        onUnstore = { chainKey, filePath ->
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()

                                val storage = Storage(settingsManager.dirPath)

                                chainListViewModel.unstore(chainKey, storage, filePath)
                                    .onSuccess {
                                        scaffoldState.snackbarHostState.showSnackbar("Unstored from ${filePath.fileName}")
                                    }
                                    .onFailure { exception ->
                                        scaffoldState.snackbarHostState.showSnackbar(exception.message ?: "Error")
                                    }
                            }
                        },
                        onDisconnect = {
                            scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()

                            settingsState.value = Settings()
                            navigationState.screenState.value = Screen.SERVER_CONNECTION

                            settingsManager.delete()
                        }
                    )
                }
                Screen.CHAIN_LINK_LIST -> {
                    ChainLinkList(
                        viewModel = chainLinkListViewModel,
                        onBack = {
                            scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()

                            chainLinkListViewModel.chain = null
                            chainLinkListViewModel.chainLinks = emptyList()

                            navigationState.screenState.value = Screen.CHAIN_LIST
                        },
                        onSync = {
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.currentSnackbarData?.performAction()

                                chainLinkListViewModel.chainLinks = emptyList()

                                chainLinkListViewModel.getAll().onFailure { exception ->
                                    scaffoldState.snackbarHostState.showSnackbar(exception.message ?: "Error")
                                }
                            }
                        },
                        onNew = { chainLink ->
                            coroutineScope.launch {
                                chainLinkListViewModel.new(chainLink).onFailure { exception ->
                                    scaffoldState.snackbarHostState.showSnackbar(exception.message ?: "Error")
                                }
                            }
                        },
                        onEdit = { chainLink ->
                            coroutineScope.launch {
                                chainLinkListViewModel.edit(chainLink).onFailure { exception ->
                                    scaffoldState.snackbarHostState.showSnackbar(exception.message ?: "Error")
                                }
                            }
                        },
                        onRemove = { chainLink ->
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

                                            scaffoldState.snackbarHostState.showSnackbar(exception.message ?: "Error")
                                        }
                                    }
                                }
                            }
                        },
                        onSearch = { scaffoldState.snackbarHostState.currentSnackbarData?.dismiss() }
                    )
                }
            }
        }
    }
}