package io.sunland.chainpass.common

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
import io.rsocket.kotlin.RSocketRequestHandler
import io.rsocket.kotlin.payload.Payload
import io.sunland.chainpass.common.network.*
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.repository.ChainRepository
import io.sunland.chainpass.common.security.PasswordGenerator
import io.sunland.chainpass.common.view.*
import io.sunland.chainpass.sqldelight.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Theme {
    enum class Palette(val color: Color) {
        ANTHRACITE(Color(0.22f, 0.24f, 0.26f)),
        QUARTZ(Color(0.91f, 0.87f, 0.88f)),
        COPPER(Color(0.72f, 0.46f, 0.28f))
    }
}

enum class Screen { SETTINGS, CHAIN_LIST, CHAIN_LINK_LIST }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App(settingsManager: SettingsManager, database: Database, storage: Storage) = MaterialTheme(
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

    val scaffoldState = rememberScaffoldState()

    val chainRepository = ChainRepository(database)
    val chainLinkRepository = ChainLinkRepository(database)

    coroutineScope.launch(Dispatchers.IO) {
        WebSocket.start(WebSocket.getLocalHost()) {
            RSocketRequestHandler {
                requestResponse { payload ->
                    when (payload.getRoute()) {
                        WebSocket.Route.CHAIN_SYNC -> {
                            Payload.encode(chainRepository.getAll().getOrThrow())
                        }
                        WebSocket.Route.CHAIN_LINK_SYNC -> {
                            Payload.encode(chainLinkRepository.getBy(payload.decode()).getOrThrow())
                        }
                    }
                }
            }
        }
    }

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
        val settingsState = remember {
            settingsManager.load()?.let { settings ->
                mutableStateOf(settings)
            } ?: run {
                val settings = Settings("", "", 16, false)

                settingsManager.save(settings)

                mutableStateOf(settings)
            }
        }

        val screenState = remember { mutableStateOf(Screen.CHAIN_LIST) }
        val loadingState = remember { mutableStateOf(false) }

        val passwordGenerator = PasswordGenerator(PasswordGenerator.Strength(
            settingsState.value.passwordLength,
            settingsState.value.passwordIsAlphanumeric
        ))

        val chainListViewModel = ChainListViewModel(chainRepository, chainLinkRepository, passwordGenerator, storage)
        val chainLinkListViewModel = ChainLinkListViewModel(chainLinkRepository)

        LaunchedEffect(settingsState.value.hostAddress) {
            settingsState.value = Settings(
                hostAddress = WebSocket.getLocalHost(),
                deviceAddress = settingsState.value.deviceAddress,
                passwordLength = settingsState.value.passwordLength,
                passwordIsAlphanumeric = settingsState.value.passwordIsAlphanumeric
            )
        }

        Box {
            when (screenState.value) {
                Screen.SETTINGS -> {
                    Settings(
                        settings = settingsState.value,
                        onBack = { settings ->
                            settingsManager.save(settings)

                            settingsState.value = settings
                            screenState.value = Screen.CHAIN_LIST
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
                        viewModel = chainListViewModel,
                        onSettings = { screenState.value = Screen.SETTINGS },
                        onSync = {
                            coroutineScope.launch {
                                if (settingsState.value.deviceAddress.isEmpty()) {
                                    scaffoldState.snackbarHostState.showSnackbar("You have to set Device Address on Settings")
                                } else runCatching {
                                    loadingState.value = true

                                    scaffoldState.snackbarHostState.currentSnackbarData?.performAction()

                                    val tcpSocket = WebSocket.connect(settingsState.value.deviceAddress)

                                    ChainApi(chainRepository, chainLinkRepository, tcpSocket).sync().onSuccess {
                                        loadingState.value = false

                                        chainListViewModel.getAll().getOrThrow()
                                    }
                                }.onFailure { exception ->
                                    loadingState.value = false

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

                                chainListViewModel.select(chain).onSuccess {
                                    chainLinkListViewModel.chain = chain

                                    screenState.value = Screen.CHAIN_LINK_LIST
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

                                chainListViewModel.store(chain, storeOptions).onSuccess { fileName ->
                                    scaffoldState.snackbarHostState.showSnackbar("Stored to $fileName")
                                }.onFailure { exception ->
                                    scaffoldState.snackbarHostState.showSnackbar(exception.message ?: "Error")
                                }
                            }
                        },
                        onUnstore = { chainKey, filePath ->
                            coroutineScope.launch {
                                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()

                                chainListViewModel.unstore(chainKey, storage, filePath).onSuccess {
                                    scaffoldState.snackbarHostState.showSnackbar("Unstored from ${filePath.fileName}")
                                }.onFailure { exception ->
                                    scaffoldState.snackbarHostState.showSnackbar(exception.message ?: "Error")
                                }
                            }
                        }
                    )
                }
                Screen.CHAIN_LINK_LIST -> {
                    coroutineScope.launch {
                        chainLinkListViewModel.getAll().onFailure { exception ->
                            scaffoldState.snackbarHostState.showSnackbar(exception.message ?: "Error")
                        }
                    }

                    ChainLinkList(
                        viewModel = chainLinkListViewModel,
                        onBack = {
                            scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()

                            chainLinkListViewModel.chain = null

                            screenState.value = Screen.CHAIN_LIST
                        },
                        onSync = { chain ->
                            coroutineScope.launch {
                                if (settingsState.value.deviceAddress.isEmpty()) {
                                    scaffoldState.snackbarHostState.showSnackbar("You have to set sync options on Settings")
                                } else runCatching {
                                    loadingState.value = true

                                    scaffoldState.snackbarHostState.currentSnackbarData?.performAction()

                                    val tcpSocket = WebSocket.connect(settingsState.value.deviceAddress)

                                    ChainLinkApi(chainLinkRepository, tcpSocket).sync(chain.id).onSuccess {
                                        loadingState.value = false

                                        chainLinkListViewModel.getAll().getOrThrow()
                                    }
                                }.onFailure { exception ->
                                    loadingState.value = false

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

            if (loadingState.value) {
                LoadingIndicator()
            }
        }
    }
}