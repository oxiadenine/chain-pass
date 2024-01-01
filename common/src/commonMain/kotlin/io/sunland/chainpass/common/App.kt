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
import io.sunland.chainpass.common.component.rememberScaffoldListState
import io.sunland.chainpass.common.network.*
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.repository.ChainRepository
import io.sunland.chainpass.common.security.PasswordGenerator
import io.sunland.chainpass.common.view.*
import io.sunland.chainpass.sqldelight.Database
import kotlinx.coroutines.*

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

class SettingsState(
    private val settingsManager: SettingsManager,
    val deviceAddressState: MutableState<String>,
    val passwordLengthState: MutableState<Int>,
    val passwordIsAlphanumericState: MutableState<Boolean>
) {
    fun save() {
        val settings = Settings(
            deviceAddressState.value,
            passwordLengthState.value,
            passwordIsAlphanumericState.value
        )

        settingsManager.save(settings)
    }
}

@Composable
fun rememberSettingsState(settingsManager: SettingsManager) = remember {
    val settings = settingsManager.load() ?: Settings("", 16, false)

    settingsManager.save(settings)

    SettingsState(
        settingsManager,
        mutableStateOf(settings.deviceAddress),
        mutableStateOf(settings.passwordLength),
        mutableStateOf(settings.passwordIsAlphanumeric)
    )
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
    val chainRepository = ChainRepository(database)
    val chainLinkRepository = ChainLinkRepository(database)

    val coroutineScope = rememberCoroutineScope()

    val navigationState = rememberNavigationState(Screen.CHAIN_LIST)
    val settingsState = rememberSettingsState(settingsManager)

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
        val hostAddressState = remember { mutableStateOf("") }

        LaunchedEffect(hostAddressState.value) {
            hostAddressState.value = WebSocket.getLocalHost()
        }

        Crossfade(targetState = navigationState.screenState.value) { screen ->
            when (screen) {
                Screen.SETTINGS -> {
                    Settings(
                        hostAddress = hostAddressState.value,
                        storePath = storage.storePath,
                        settingsState = settingsState,
                        navigationState = navigationState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Screen.CHAIN_LIST -> {
                    val chainListViewModel = ChainListViewModel(
                        chainRepository,
                        chainLinkRepository,
                        PasswordGenerator(PasswordGenerator.Strength(
                            settingsState.passwordLengthState.value,
                            settingsState.passwordIsAlphanumericState.value
                        )),
                        storage
                    )

                    val scaffoldListState = rememberScaffoldListState()

                    ChainScaffoldList(
                        viewModel = chainListViewModel,
                        settingsState = settingsState,
                        navigationState = navigationState,
                        scaffoldListState = scaffoldListState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Screen.CHAIN_LINK_LIST -> {
                    val chainLinkListViewModel = ChainLinkListViewModel(chainLinkRepository, storage).apply {
                        chain = navigationState.chainState.value
                    }

                    val scaffoldListState = rememberScaffoldListState()

                    ChainLinkScaffoldList(
                        viewModel = chainLinkListViewModel,
                        settingsState = settingsState,
                        navigationState = navigationState,
                        scaffoldListState = scaffoldListState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}