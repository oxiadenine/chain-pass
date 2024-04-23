package io.github.oxiadenine.chainpass

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.oxiadenine.chainpass.component.NavigationHost
import io.github.oxiadenine.chainpass.component.NavigationState
import io.github.oxiadenine.chainpass.repository.ChainLinkRepository
import io.github.oxiadenine.chainpass.repository.ChainRepository
import io.github.oxiadenine.chainpass.security.PasswordGenerator
import io.github.oxiadenine.chainpass.view.*
import io.github.oxiadenine.common.generated.resources.Res
import io.github.oxiadenine.common.generated.resources.drawer_item_settings_text
import io.github.oxiadenine.common.generated.resources.drawer_network_text
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import java.io.File
import java.util.*

class SettingsState(private val filePath: String) {
    val deviceAddressState = mutableStateOf("")
    val passwordLengthState = mutableStateOf(16)
    val passwordIsAlphanumericState = mutableStateOf(false)
    val languageState = mutableStateOf("")

    @Serializable
    data class Settings(
        val deviceAddress: String,
        val passwordLength: Int,
        val passwordIsAlphanumeric: Boolean,
        val language: String
    )

    init {
        val settingsFile = File(filePath)

        if (settingsFile.exists()) {
            val settings = Json.decodeFromString<Settings>(settingsFile.readText())

            deviceAddressState.value = settings.deviceAddress
            passwordLengthState.value = settings.passwordLength
            passwordIsAlphanumericState.value = settings.passwordIsAlphanumeric
            languageState.value = settings.language
        } else {
            settingsFile.createNewFile()

            save()
        }
    }

    fun save() {
        val settings = Settings(
            deviceAddressState.value,
            passwordLengthState.value,
            passwordIsAlphanumericState.value,
            languageState.value
        )

        File(filePath).writeText(Json.encodeToString(settings))
    }
}

@Composable
fun rememberSettingsState(dirPath: String) = remember { SettingsState(dirPath) }

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

object Intl {
    val languages = listOf("es", "en")
}

val LocalLocale = staticCompositionLocalOf { Locale.getDefault() }

@Composable
fun App(
    chainRepository: ChainRepository,
    chainLinkRepository: ChainLinkRepository,
    settingsState: SettingsState,
    networkState: NetworkState,
    themeState: ThemeState,
    navigationState: NavigationState
) {
    val coroutineScope = rememberCoroutineScope()

    if (settingsState.languageState.value.isEmpty()) {
        settingsState.languageState.value = Intl.languages.first { language ->
            language == Locale.getDefault().language
        }
    } else Locale.setDefault(Locale(settingsState.languageState.value))

    CompositionLocalProvider(LocalLocale provides Locale.getDefault()) {
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
                                            modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
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
                                            text = hostAddress.ifEmpty { stringResource(Res.string.drawer_network_text) },
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                NavigationDrawerItem(
                                    label = { Text(text = stringResource(Res.string.drawer_item_settings_text)) },
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
                                        .pointerHoverIcon(icon = PointerIcon.Hand)
                                )
                            }
                        },
                        drawerState = drawerState,
                        scrimColor = Color.Black.copy(alpha = if (platform == Platform.DESKTOP) 0.3f else 0.6f)
                    ) {
                        val chainListViewModel = rememberChainListViewModel(chainRepository, chainLinkRepository)

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
                            deviceAddress = settingsState.deviceAddressState.value,
                            passwordGenerator = PasswordGenerator(
                                PasswordGenerator.Strength(
                                settingsState.passwordLengthState.value,
                                settingsState.passwordIsAlphanumericState.value
                            ))
                        )
                    }

                    if (settingsDialogVisible) {
                        SettingsDialog(
                            settingsState = settingsState,
                            onClose = {
                                Locale.setDefault(Locale(settingsState.languageState.value))

                                settingsState.save()

                                settingsDialogVisible = false
                            },
                            storeDirPath = chainRepository.storage.storeDirPath
                        )
                    }
                }
                composableRoute<ChainLinkListRouteArgument>(route = Screen.CHAIN_LINK_LIST.name) { argument ->
                    val chainLinkListViewModel = rememberChainLinkListViewModel(chainLinkRepository, argument!!.chain)

                    chainLinkListViewModel.chainLinkSelected = argument.chainLink

                    ChainLinkList(
                        viewModel = chainLinkListViewModel,
                        onTopAppBarBackClick = { navigationState.pop() },
                        onTopAppBarSearchClick = { chainLinks ->
                            navigationState.push(
                                NavigationState.Route(
                                path = Screen.CHAIN_LINK_SEARCH_LIST.name,
                                argument = ChainLinkSearchListRouteArgument(chainLinks),
                                animation = tween(easing = FastOutLinearInEasing)
                            ))
                        },
                        deviceAddress = settingsState.deviceAddressState.value,
                        passwordGenerator = PasswordGenerator(
                            PasswordGenerator.Strength(
                            settingsState.passwordLengthState.value,
                            settingsState.passwordIsAlphanumericState.value
                        ))
                    )
                }
                composableRoute<ChainLinkSearchListRouteArgument>(Screen.CHAIN_LINK_SEARCH_LIST.name) { argument ->
                    val chainLinkSearchListState = rememberChainLinkSearchListState(chainLinks = argument!!.chainLinks)

                    ChainLinkSearchList(
                        state = chainLinkSearchListState,
                        onTopAppBarBackClick = { navigationState.pop() },
                        onListItemClick = { chainLink ->
                            navigationState.pop(
                                NavigationState.Route(
                                argument = ChainLinkListRouteArgument(chainLink.chain, chainLink)
                            ))
                        }
                    )
                }
            }
        }
    }
}