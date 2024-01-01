package io.sunland.chainpass.desktop

import androidx.compose.foundation.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.sunland.chainpass.common.*
import java.awt.Dimension

fun main() {
    val settingsManager = SettingsManager("${System.getProperty("user.home")}/.chain-pass")
    val database = DatabaseFactory.createDatabase(DriverFactory(settingsManager.dirPath))
    val storage = Storage("${System.getProperty("user.home")}/Downloads")

    application {
        Window(
            icon = painterResource("icon.png"),
            title = "Chain Pass",
            onCloseRequest = ::exitApplication
        ) {
            window.minimumSize = Dimension(360, 480)

            val themeState = rememberThemeState(ThemeMode.DARK)

            CompositionLocalProvider(
                LocalContextMenuRepresentation provides if (themeState.isDarkMode) {
                    DarkDefaultContextMenuRepresentation
                } else LightDefaultContextMenuRepresentation
            ) {
                MaterialTheme(colorScheme = if (themeState.isDarkMode) {
                    Theme.DarkColors
                } else Theme.LightColors) {
                    App(settingsManager, database, storage, themeState)
                }
            }
        }
    }
}