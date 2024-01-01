package io.sunland.chainpass.common

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.server.cio.*
import io.ktor.server.engine.*
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
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

object Theme {
    enum class Palette(val color: Color) {
        ANTHRACITE(Color(0.22f, 0.24f, 0.26f)),
        QUARTZ(Color(0.91f, 0.87f, 0.88f)),
        COPPER(Color(0.72f, 0.46f, 0.28f))
    }
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

    ModalDrawer(
        drawerState = drawerState,
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

            Column(
                modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Text(text = platform)
                Text(text = networkState.hostAddressState.value.ifEmpty { "Not connected" }, fontSize = 14.sp)
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        coroutineScope.launch {
                            drawerState.close()

                            isSettingsDialogVisibleState.value = true
                        }
                    }.pointerHoverIcon(icon = PointerIconDefaults.Hand),
                    horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.padding(start = 16.dp),
                        imageVector = Icons.Default.Settings,
                        contentDescription = null
                    )
                    Text(text = "Settings", modifier = Modifier.padding(vertical = 16.dp), fontSize = 14.sp)
                }
            }
        },
        drawerShape = object : Shape {
            override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
                return with(density) {
                    Outline.Rectangle(Rect(0f,0f, 300.dp.roundToPx().toFloat(), size.height))
                }
            }
        },
        drawerBackgroundColor = MaterialTheme.colors.surface,
        scrimColor = Color.Black.copy(alpha = 0.5f)
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

                        val scaffoldListState = rememberScaffoldListState()

                        ChainScaffoldList(
                            viewModel = chainListViewModel,
                            settingsState = settingsState,
                            navigationState = navigationState,
                            drawerState = drawerState,
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
}