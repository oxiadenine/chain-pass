package io.github.oxiadenine.chainpass

import androidx.compose.foundation.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.oxiadenine.chainpass.network.SyncServer
import io.github.oxiadenine.chainpass.component.rememberNavigationState
import io.github.oxiadenine.chainpass.repository.ChainLinkRepository
import io.github.oxiadenine.chainpass.repository.ChainRepository
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.awt.Dimension
import java.io.File

private object ResourceLoader

private fun readResourceBytes(resourcePath: String) =
    ResourceLoader.javaClass.classLoader.getResourceAsStream(resourcePath)!!.readAllBytes()

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun rememberBitmapResource(path: String) = remember(path) {
    BitmapPainter(readResourceBytes(path).decodeToImageBitmap())
}

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
    val settings = Settings(appDataDir.absolutePath)
    val storage = Storage(appStorageDir.absolutePath)

    val chainRepository = ChainRepository(database, storage)
    val chainLinkRepository = ChainLinkRepository(database, storage)

    val syncServer = SyncServer(chainRepository, chainLinkRepository).start()

    application {
        val windowState = rememberWindowState()
        val navigationState = rememberNavigationState()

        windowState.size = DpSize(640.dp, 480.dp)
        windowState.position = WindowPosition(Alignment.Center)

        Window(
            state = windowState,
            onCloseRequest = ::exitApplication,
            title = "Chain Pass",
            icon = rememberBitmapResource("icon.png"),
            onKeyEvent = { keyEvent ->
                if (keyEvent.isShiftPressed && keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Escape) {
                    if (navigationState.composableRouteStack.size > 1) {
                        navigationState.pop()
                    } else exitApplication()

                    true
                } else false
            }
        ) {
            window.minimumSize = Dimension(360, 480)

            val settingsState = rememberSettingsState(settings)
            val networkState = rememberNetworkState(syncServer.hostAddressFlow)
            val themeState = rememberThemeState(ThemeMode.DARK)

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