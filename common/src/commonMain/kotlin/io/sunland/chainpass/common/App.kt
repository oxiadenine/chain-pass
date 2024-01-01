package io.sunland.chainpass.common

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import io.rsocket.kotlin.RSocketRequestHandler
import io.rsocket.kotlin.payload.Payload
import io.sunland.chainpass.common.component.PopupHostState
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

class NavigationState(val screenState: MutableState<Screen>, val chainState: MutableState<Chain?>)

@Composable
fun rememberNavigationState(screen: Screen) = remember {
    NavigationState(mutableStateOf(screen), mutableStateOf(null))
}

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

    val navigationState = rememberNavigationState(Screen.CHAIN_LIST)

    val chainRepository = ChainRepository(database)
    val chainLinkRepository = ChainLinkRepository(database)

    coroutineScope.launch(Dispatchers.IO) {
        WebSocket.start(WebSocket.getLocalHost()) {
            RSocketRequestHandler {
                requestResponse { payload ->
                    when (payload.getRoute()) {
                        WebSocket.Route.CHAIN_SYNC -> Payload.encode(chainRepository.getAll())
                        WebSocket.Route.CHAIN_LINK_SYNC -> Payload.encode(chainLinkRepository.getBy(payload.decode()))
                    }
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        val settingsState = remember {
            settingsManager.load()?.let { settings ->
                mutableStateOf(settings)
            } ?: run {
                val settings = Settings(
                    hostAddress = "",
                    deviceAddress = "",
                    passwordLength = 16,
                    passwordIsAlphanumeric =  false,
                    storePath = ""
                )

                settingsManager.save(settings)

                mutableStateOf(settings)
            }
        }

        LaunchedEffect(settingsState.value.hostAddress) {
            settingsState.value = Settings(
                hostAddress = WebSocket.getLocalHost(),
                deviceAddress = settingsState.value.deviceAddress,
                passwordLength = settingsState.value.passwordLength,
                passwordIsAlphanumeric = settingsState.value.passwordIsAlphanumeric,
                storePath = storage.storePath
            )
        }

        Crossfade(targetState = navigationState.screenState.value) { screen ->
            when (screen) {
                Screen.SETTINGS -> {
                    Settings(
                        settings = settingsState.value,
                        onBack = { navigationState.screenState.value = Screen.CHAIN_LIST },
                        onSave = { settings ->
                            settingsManager.save(settings)

                            settingsState.value = settings
                            navigationState.screenState.value = Screen.CHAIN_LIST
                        }
                    )
                }
                Screen.CHAIN_LIST -> {
                    val chainListViewModel = ChainListViewModel(
                        chainRepository,
                        chainLinkRepository,
                        PasswordGenerator(PasswordGenerator.Strength(
                            settingsState.value.passwordLength,
                            settingsState.value.passwordIsAlphanumeric
                        )),
                        storage
                    )

                    ChainList(
                        viewModel = chainListViewModel,
                        settingsState = settingsState,
                        navigationState = navigationState,
                        snackbarHostState = SnackbarHostState(),
                        popupHostState = PopupHostState()
                    )
                }
                Screen.CHAIN_LINK_LIST -> {
                    val chainLinkListViewModel = ChainLinkListViewModel(chainLinkRepository, storage).apply {
                        chain = navigationState.chainState.value
                    }

                    ChainLinkList(
                        viewModel = chainLinkListViewModel,
                        settingsState = settingsState,
                        navigationState = navigationState,
                        snackbarHostState = SnackbarHostState(),
                        popupHostState = PopupHostState()
                    )
                }
            }
        }
    }
}