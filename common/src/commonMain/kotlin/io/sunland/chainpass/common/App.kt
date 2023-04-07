package io.sunland.chainpass.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import io.sunland.chainpass.common.component.NavigationHost
import io.sunland.chainpass.common.component.NavigationState
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.repository.ChainRepository
import io.sunland.chainpass.common.security.PasswordGenerator
import io.sunland.chainpass.common.view.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsState(
    private val settingsManager: SettingsManager,
    val deviceAddressState: MutableState<String>,
    val passwordLengthState: MutableState<Int>,
    val passwordIsAlphanumericState: MutableState<Boolean>,
    val languageState: MutableState<String>
) {
    fun save() {
        val settings = Settings(
            deviceAddressState.value,
            passwordLengthState.value,
            passwordIsAlphanumericState.value,
            languageState.value
        )

        settingsManager.save(settings)
    }
}

@Composable
fun rememberSettingsState(settingsManager: SettingsManager) = remember {
    val settings = settingsManager.load() ?: Settings("", 16, false, "en")

    settingsManager.save(settings)

    SettingsState(
        settingsManager,
        mutableStateOf(settings.deviceAddress),
        mutableStateOf(settings.passwordLength),
        mutableStateOf(settings.passwordIsAlphanumeric),
        mutableStateOf(settings.language)
    )
}

class NetworkState(val hostAddressState: State<String>)

@Composable
fun rememberNetworkState(hostAddressFlow: Flow<String>): NetworkState {
    val hostAddressState = hostAddressFlow.collectAsState("")

    return remember { NetworkState(hostAddressState) }
}

enum class ThemeMode { DARK, LIGHT }

class ThemeState(val mode: MutableState<ThemeMode>) {
    val isDarkMode by derivedStateOf { mode.value == ThemeMode.DARK }
}

@Composable
fun rememberThemeState(mode: ThemeMode = ThemeMode.LIGHT) = remember {
    ThemeState(mutableStateOf(mode))
}

enum class Screen { CHAIN_LIST, CHAIN_LINK_LIST, CHAIN_LINK_SEARCH_LIST }

val LocalIntl = staticCompositionLocalOf { Intl("en") }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun App(
    chainRepository: ChainRepository,
    chainLinkRepository: ChainLinkRepository,
    settingsState: SettingsState,
    networkState: NetworkState,
    themeState: ThemeState,
    navigationState: NavigationState,
    storePath: String
) {
    CompositionLocalProvider(LocalIntl provides Intl(settingsState.languageState.value)) {
        val intl = LocalIntl.current

        val coroutineScope = rememberCoroutineScope()

        Surface(modifier = Modifier.fillMaxSize()) {
            NavigationHost(navigationState = navigationState, initialRoute = Screen.CHAIN_LIST.name) {
                composableRoute<ChainListRouteArgument>(route = Screen.CHAIN_LIST.name) {
                    val drawerState = rememberDrawerState(DrawerValue.Closed)

                    var settingsDialogVisible by remember { mutableStateOf(false) }

                    ModalNavigationDrawer(
                        drawerContent = {
                            ModalDrawerSheet(
                                modifier = Modifier.width(width = 300.dp),
                                drawerShape = if (platform == Platform.DESKTOP) {
                                    RectangleShape
                                } else DrawerDefaults.shape
                            ) {
                                val hostAddress = networkState.hostAddressState.value

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
                                        Text(text = System.getProperty("os.name")?.let { osName ->
                                            if (platform == Platform.ANDROID) {
                                                platform.name.lowercase().replaceFirstChar { it.uppercase() }
                                            } else osName
                                        } ?: platform.name.lowercase().replaceFirstChar { it.uppercase() })
                                        Text(
                                            text = hostAddress.ifEmpty { intl.translate("drawer.network.text") },
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                NavigationDrawerItem(
                                    label = { Text(text = intl.translate("drawer.item.settings.text")) },
                                    selected = false,
                                    onClick = {
                                        coroutineScope.launch {
                                            drawerState.close()

                                            settingsDialogVisible = true
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
                        val chainListViewModel = rememberChainListViewModel(
                            passwordGenerator = PasswordGenerator(
                                PasswordGenerator.Strength(
                                    settingsState.passwordLengthState.value,
                                    settingsState.passwordIsAlphanumericState.value
                                )),
                            chainRepository = chainRepository,
                            chainLinkRepository = chainLinkRepository
                        )

                        ChainList(
                            viewModel = chainListViewModel,
                            onTopAppBarMenuClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            },
                            onListItemOpenMenuItemClick = { chain ->
                                navigationState.push(route = NavigationState.Route(
                                    path = Screen.CHAIN_LINK_LIST.name,
                                    argument = ChainLinkListRouteArgument(chain),
                                    animation = tween(easing = FastOutSlowInEasing)
                                ))
                            },
                            deviceAddress = settingsState.deviceAddressState.value
                        )
                    }

                    if (settingsDialogVisible) {
                        SettingsDialog(
                            settingsState = settingsState,
                            onClose = { settingsDialogVisible = false },
                            storePath = storePath
                        )
                    }
                }

                composableRoute<ChainLinkListRouteArgument>(route = Screen.CHAIN_LINK_LIST.name) { argument ->
                    val chainLinkListViewModel = rememberChainLinkListViewModel(
                        passwordGenerator = PasswordGenerator(
                            PasswordGenerator.Strength(
                                settingsState.passwordLengthState.value,
                                settingsState.passwordIsAlphanumericState.value
                            )),
                        chainLinkRepository = chainLinkRepository,
                        chain = argument!!.chain
                    )

                    chainLinkListViewModel.chainLinkSelected = argument.chainLink

                    ChainLinkList(
                        viewModel = chainLinkListViewModel,
                        onTopAppBarBackClick = { navigationState.pop() },
                        onTopAppBarSearchClick = { chainLinks ->
                            navigationState.push(NavigationState.Route(
                                path = Screen.CHAIN_LINK_SEARCH_LIST.name,
                                argument = ChainLinkSearchListRouteArgument(chainLinks),
                                animation = tween(easing = FastOutLinearInEasing)
                            ))
                        },
                        deviceAddress = settingsState.deviceAddressState.value
                    )
                }
                composableRoute<ChainLinkSearchListRouteArgument>(Screen.CHAIN_LINK_SEARCH_LIST.name) { argument ->
                    val chainLinkSearchListState = rememberChainLinkSearchListState(chainLinks = argument!!.chainLinks)

                    ChainLinkSearchList(
                        state = chainLinkSearchListState,
                        onTopAppBarBackClick = { navigationState.pop() },
                        onListItemClick = { chainLink ->
                            navigationState.pop(NavigationState.Route(
                                argument = ChainLinkListRouteArgument(chainLink.chain, chainLink)
                            ))
                        }
                    )
                }
            }
        }
    }
}