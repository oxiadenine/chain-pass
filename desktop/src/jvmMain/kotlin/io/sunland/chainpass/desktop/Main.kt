package io.sunland.chainpass.desktop

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

            App(settingsManager, database, storage)
        }
    }
}