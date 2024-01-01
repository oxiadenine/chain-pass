package io.sunland.chainpass.desktop

import androidx.compose.foundation.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.component.rememberNavigationState
import io.sunland.chainpass.common.network.*
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.repository.ChainRepository
import java.awt.Dimension

fun main() {
    val settingsManager = SettingsManager("${System.getProperty("user.home")}/.chain-pass")
    val database = DatabaseFactory.createDatabase(DriverFactory(settingsManager.dirPath))
    val storage = Storage("${System.getProperty("user.home")}/Downloads")

    val chainRepository = ChainRepository(database, storage)
    val chainLinkRepository = ChainLinkRepository(database, storage)

    val syncServer = SyncServer(chainRepository, chainLinkRepository).start()

    application {
        Window(
            icon = painterResource("icon.png"),
            title = "Chain Pass",
            onCloseRequest = ::exitApplication
        ) {
            window.minimumSize = Dimension(360, 480)

            val settingsState = rememberSettingsState(settingsManager)
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
                        navigationState = navigationState,
                        storePath = storage.storePath
                    )
                }
            }
        }
    }
}