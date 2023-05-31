package io.sunland.chainpass

import androidx.compose.foundation.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.component.rememberNavigationState
import io.sunland.chainpass.common.network.*
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.repository.ChainRepository
import java.awt.Dimension
import java.io.File

fun main() {
    val appDataDir = if (System.getenv("DEBUG")?.toBoolean() ?: false) {
        File("${System.getProperty("user.home")}/.chain-pass/local")
    } else File("${System.getProperty("user.home")}/.chain-pass")

    val appStorageDir = File("${System.getProperty("user.home")}/Downloads/Chain Pass")

    if (!appDataDir.exists()) {
        appDataDir.mkdirs()
    }

    if (!appStorageDir.exists()) {
        appStorageDir.mkdirs()
    }

    val database = Database.create(appDataDir.absolutePath)
    val storage = Storage("${appStorageDir.absolutePath}/Store")

    val chainRepository = ChainRepository(database, storage)
    val chainLinkRepository = ChainLinkRepository(database, storage)

    val syncServer = SyncServer(chainRepository, chainLinkRepository).start()

    application {
        val windowState = rememberWindowState()

        windowState.size = DpSize(640.dp, 480.dp)
        windowState.position = WindowPosition(Alignment.Center)

        Window(
            state = windowState,
            onCloseRequest = ::exitApplication,
            title = "Chain Pass",
            icon = painterResource("icon.png")
        ) {
            window.minimumSize = Dimension(360, 480)

            val settingsState = rememberSettingsState("${appDataDir.absolutePath}/settings.json")
            val networkState = rememberNetworkState(syncServer.hostAddressFlow)
            val themeState = rememberThemeState(ThemeMode.DARK)
            val navigationState = rememberNavigationState()

            CompositionLocalProvider(
                LocalContextMenuRepresentation provides if (themeState.isDarkMode) {
                    DarkDefaultContextMenuRepresentation
                } else LightDefaultContextMenuRepresentation
            ) {
                MaterialTheme(colorScheme = if (themeState.isDarkMode) {
                    Theme.DarkColors
                } else Theme.LightColors) {
                    App(
                        chainRepository = chainRepository,
                        chainLinkRepository = chainLinkRepository,
                        settingsState = settingsState,
                        networkState = networkState,
                        themeState = themeState,
                        navigationState = navigationState
                    )
                }
            }
        }
    }
}