package com.github.sunland.chainpass

import android.os.Bundle
import android.os.Environment
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import com.github.sunland.chainpass.network.SyncServer
import com.github.sunland.chainpass.component.rememberNavigationState
import com.github.sunland.chainpass.repository.ChainLinkRepository
import com.github.sunland.chainpass.repository.ChainRepository
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appDataDir = if (BuildConfig.DEBUG) {
            File("${applicationContext.getExternalFilesDir("")!!.absolutePath}/local")
        } else File(applicationContext.getExternalFilesDir("")!!.absolutePath)

        val appStorageDir = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path}/Chain Pass")

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

        setContent {
            title = "Chain Pass"

            val settingsState = rememberSettingsState("${appDataDir.absolutePath}/settings.json")
            val networkState = rememberNetworkState(syncServer.hostAddressFlow)
            val themeState = rememberThemeState(ThemeMode.DARK)
            val navigationState = rememberNavigationState()

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