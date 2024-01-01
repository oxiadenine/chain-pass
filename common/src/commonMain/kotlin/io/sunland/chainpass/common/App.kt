package io.sunland.chainpass.common

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.server.cio.*
import io.rsocket.kotlin.RSocketRequestHandler
import io.rsocket.kotlin.payload.Payload
import io.sunland.chainpass.common.network.WebSocket
import io.sunland.chainpass.common.network.decode
import io.sunland.chainpass.common.network.encode
import io.sunland.chainpass.common.network.getRoute
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.repository.ChainRepository
import io.sunland.chainpass.common.security.PasswordGenerator
import io.sunland.chainpass.common.view.*
import io.sunland.chainpass.sqldelight.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

enum class ThemeMode { DARK, LIGHT }

class ThemeState(val mode: MutableState<ThemeMode>) {
    val isDarkMode by derivedStateOf { mode.value == ThemeMode.DARK }
}

@Composable
fun rememberThemeState(mode: ThemeMode = ThemeMode.LIGHT) = remember {
    ThemeState(mutableStateOf(mode))
}

enum class Screen { CHAIN_LIST, CHAIN_LINK_LIST }

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

class NetworkState(var hostAddressState: State<String>) {
    val hostAddressFlow = flow {
        while (true) {
            delay(1000)

            emit(WebSocket.getLocalHost().getOrElse { "" })
        }
    }
}

@Composable
fun rememberNetworkState(): NetworkState {
    val networkState = NetworkState(mutableStateOf(""))

    networkState.hostAddressState = networkState.hostAddressFlow
        .flowOn(Dispatchers.IO)
        .distinctUntilChanged()
        .conflate()
        .collectAsState("")

    return remember { networkState }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun App(settingsManager: SettingsManager, database: Database, storage: Storage, themeState: ThemeState) {
    val chainRepository = ChainRepository(database)
    val chainLinkRepository = ChainLinkRepository(database)

    val coroutineScope = rememberCoroutineScope()

    val navigationState = rememberNavigationState(Screen.CHAIN_LIST)
    val settingsState = rememberSettingsState(settingsManager)
    val networkState = rememberNetworkState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val isSettingsDialogVisibleState = remember { mutableStateOf(false) }

    if (isSettingsDialogVisibleState.value) {
        SettingsDialog(
            storePath = storage.storePath,
            settingsState = settingsState,
            onClose = { isSettingsDialogVisibleState.value = false }
        )
    }

    ModalNavigationDrawer(
        drawerContent = {
            val socketServerState = mutableStateOf<CIOApplicationEngine?>(null)

            if (networkState.hostAddressState.value.isNotEmpty()) {
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        socketServerState.value = WebSocket.start(networkState.hostAddressState.value) {
                            RSocketRequestHandler {
                                requestResponse { payload ->
                                    when (payload.getRoute()) {
                                        WebSocket.Route.CHAIN_SYNC -> Payload.encode(chainRepository.getAll())
                                        WebSocket.Route.CHAIN_LINK_SYNC -> Payload.encode(
                                            chainLinkRepository.getBy(
                                                payload.decode()
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    } catch (_: Throwable) {}
                }
            } else {
                socketServerState.value?.application?.cancel()
                socketServerState.value?.application?.dispose()
            }

            ModalDrawerSheet(drawerShape = if (platform == Platform.DESKTOP) {
                RectangleShape
            } else DrawerDefaults.shape) {
                Box(modifier = Modifier.fillMaxWidth().padding(all = 4.dp)) {
                    Row(modifier = Modifier.align(alignment = Alignment.TopEnd)) {
                        IconButton(
                            onClick = {
                                themeState.mode.value = if (themeState.isDarkMode) {
                                    ThemeMode.LIGHT
                                } else ThemeMode.DARK
                            },
                            modifier = Modifier.pointerHoverIcon(icon = PointerIconDefaults.Hand)
                        ) {
                            AnimatedVisibility(
                                visible = themeState.isDarkMode,
                                enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 500))
                            ) { Icon(imageVector = Icons.Default.LightMode, contentDescription = null) }
                            AnimatedVisibility(
                                visible = !themeState.isDarkMode,
                                enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                                exit = fadeOut(animationSpec = tween(durationMillis = 500))
                            ) { Icon(imageVector = Icons.Default.DarkMode, contentDescription = null) }
                        }
                    }
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(space = 4.dp)
                    ) {
                        Text(text = platform.name.lowercase().replaceFirstChar { char -> char.uppercase() })
                        Text(text = networkState.hostAddressState.value.ifEmpty { "Not connected" }, fontSize = 14.sp)
                    }
                }
                NavigationDrawerItem(
                    label = { Text(text = "Settings") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()

                            isSettingsDialogVisibleState.value = true
                        }
                    },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier
                        .padding(paddingValues = NavigationDrawerItemDefaults.ItemPadding)
                        .pointerHoverIcon(icon = PointerIconDefaults.Hand)
                )
            }
        },
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = if (platform == Platform.DESKTOP) 0.3f else 0.6f)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Crossfade(targetState = navigationState.screenState.value) { screen ->
                when (screen) {
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

                        ChainScaffoldList(
                            viewModel = chainListViewModel,
                            settingsState = settingsState,
                            navigationState = navigationState,
                            drawerState = drawerState
                        )
                    }
                    Screen.CHAIN_LINK_LIST -> {
                        val chainLinkListViewModel = ChainLinkListViewModel(chainLinkRepository, storage).apply {
                            chain = navigationState.chainState.value
                        }

                        ChainLinkScaffoldList(
                            viewModel = chainLinkListViewModel,
                            settingsState = settingsState,
                            navigationState = navigationState,
                        )
                    }
                }
            }
        }
    }
}