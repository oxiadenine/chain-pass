package io.github.oxiadenine.chainpass

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
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
import kotlinx.serialization.json.*
import org.jetbrains.compose.resources.stringResource
import java.util.*

data class SettingsState(
    val deviceAddress: String = "",
    val passwordLength: Int = 16,
    val passwordIsAlphanumeric: Boolean = false,
    val language: String = ""
) {
    fun toJson() = buildJsonObject {
        put("deviceAddress", deviceAddress)
        put("passwordLength", passwordLength)
        put("passwordIsAlphanumeric", passwordIsAlphanumeric)
        put("language", language)
    }

    companion object {
        val Saver = mapSaver(
            save = { state ->
                mapOf(
                    "deviceAddress" to state.value.deviceAddress,
                    "passwordLength" to state.value.passwordLength,
                    "passwordIsAlphanumeric" to state.value.passwordIsAlphanumeric,
                    "language" to state.value.language
                )
            },
            restore = {
                mutableStateOf(SettingsState(
                    it["deviceAddress"] as String,
                    it["passwordLength"] as Int,
                    it["passwordIsAlphanumeric"] as Boolean,
                    it["language"] as String
                ))
            }
        )

        fun fromJson(settingsJson: JsonObject) = SettingsState(
            deviceAddress = settingsJson["deviceAddress"]!!.jsonPrimitive.content,
            passwordLength = settingsJson["passwordLength"]!!.jsonPrimitive.content.toInt(),
            passwordIsAlphanumeric = settingsJson["passwordIsAlphanumeric"]!!.jsonPrimitive.content.toBoolean(),
            language = settingsJson["language"]!!.jsonPrimitive.content
        )
    }
}

@Composable
fun rememberSettingsState(settings: Settings) = rememberSaveable(saver = SettingsState.Saver) {
    mutableStateOf(settings.load()?.let { settingsJson ->
        SettingsState.fromJson(settingsJson)
    } ?: run {
        val settingsState = SettingsState(language = Locale.getDefault().language)

        settings.save(settingsState.toJson())

        settingsState
    })
}

class NetworkState(val hostAddressState: State<String>)

@Composable
fun rememberNetworkState(hostAddressFlow: Flow<String>): NetworkState {
    val hostAddressState = hostAddressFlow.collectAsState("")

    return remember { NetworkState(hostAddressState) }
}

data class Screen(val width: Dp = Dp.Unspecified, val height: Dp = Dp.Unspecified)

val LocalScreen = staticCompositionLocalOf { Screen() }

enum class ThemeMode { DARK, LIGHT }

class ThemeState(val mode: MutableState<ThemeMode>) {
    val isDarkMode by derivedStateOf { mode.value == ThemeMode.DARK }
}

@Composable
fun rememberThemeState(mode: ThemeMode = ThemeMode.LIGHT) = remember {
    ThemeState(mutableStateOf(mode))
}

object Route {
    @Serializable
    data object ChainList
    @Serializable
    data class ChainLinkList(val chainId: String, val chainKey: String)
}

object Intl {
    val languages = listOf("es", "en")
}

val LocalLocale = staticCompositionLocalOf { Locale.getDefault() }

@Composable
fun App(
    chainRepository: ChainRepository,
    chainLinkRepository: ChainLinkRepository,
    settings: Settings,
    networkState: NetworkState,
    themeState: ThemeState,
    navHostController: NavHostController
) {
    val coroutineScope = rememberCoroutineScope()

    var settingsState by rememberSettingsState(settings)

    Locale.setDefault(Locale(settingsState.language))

    CompositionLocalProvider(LocalLocale provides Locale.getDefault()) {
        Surface(modifier = Modifier.safeContentPadding().fillMaxSize()) {
            NavHost(navController = navHostController, startDestination = Route.ChainList) {
                composable<Route.ChainList> {
                    val drawerState = rememberDrawerState(DrawerValue.Closed)

                    var settingsDialogVisible by rememberSaveable { mutableStateOf(false) }

                    if (settingsDialogVisible) {
                        SettingsDialog(
                            settingsState = settingsState,
                            onSave = { newSettingsState ->
                                settingsState = newSettingsState

                                settings.save(settingsState.toJson())

                                Locale.setDefault(Locale(settingsState.language))

                                settingsDialogVisible = false
                            },
                            onClose = { settingsDialogVisible = false },
                            storeDirPath = chainRepository.storage.storeDir.absolutePath,
                            languages = Intl.languages
                        )
                    }

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
                                            ) {
                                                Icon(imageVector = Icons.Default.LightMode, contentDescription = null)
                                            }
                                            AnimatedVisibility(
                                                visible = !themeState.isDarkMode,
                                                enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                                                exit = fadeOut(animationSpec = tween(durationMillis = 500))
                                            ) {
                                                Icon(imageVector = Icons.Default.DarkMode, contentDescription = null)
                                            }
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
                                            text = hostAddress.ifEmpty {
                                                stringResource(Res.string.drawer_network_text)
                                            },
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
                        ChainList(
                            viewModel = viewModel { ChainListViewModel(chainRepository, chainLinkRepository) },
                            onTopAppBarMenuClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            },
                            onListItemOpenMenuItemClick = { chain ->
                                coroutineScope.launch {
                                    navHostController.navigate(route = Route.ChainLinkList(chain.id, chain.key.value))
                                }
                            },
                            deviceAddress = settingsState.deviceAddress,
                            passwordGenerator = PasswordGenerator(PasswordGenerator.Strength(
                                settingsState.passwordLength,
                                settingsState.passwordIsAlphanumeric
                            ))
                        )
                    }
                }
                composable<Route.ChainLinkList> { navBackStackEntry ->
                    val route = navBackStackEntry.toRoute<Route.ChainLinkList>()

                    ChainLinkList(
                        chainId = route.chainId,
                        chainKey = Chain.Key(route.chainKey),
                        viewModel = viewModel { ChainLinkListViewModel(chainRepository, chainLinkRepository) },
                        onTopAppBarBackClick = { navHostController.navigateUp() },
                        deviceAddress = settingsState.deviceAddress,
                        passwordGenerator = PasswordGenerator(PasswordGenerator.Strength(
                            settingsState.passwordLength,
                            settingsState.passwordIsAlphanumeric
                        ))
                    )
                }
            }
        }
    }
}