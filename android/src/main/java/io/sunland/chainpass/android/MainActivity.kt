package io.sunland.chainpass.android

import android.os.Bundle
import android.os.Environment
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import io.sunland.chainpass.common.*
import io.sunland.chainpass.common.component.rememberNavigationState
import io.sunland.chainpass.common.network.*
import io.sunland.chainpass.common.repository.ChainLinkRepository
import io.sunland.chainpass.common.repository.ChainRepository

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsManager = SettingsManager(applicationContext.getExternalFilesDir("")!!.absolutePath)
        val database = DatabaseFactory.createDatabase(DriverFactory(applicationContext))
        val storage = Storage(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path)

        val chainRepository = ChainRepository(database, storage)
        val chainLinkRepository = ChainLinkRepository(database, storage)

        val syncServer = SyncServer(chainRepository, chainLinkRepository).start()

        setContent {
            title = "Chain Pass"

            val settingsState = rememberSettingsState(settingsManager)
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
                    navigationState = navigationState,
                    storePath = storage.storePath
                )
            }
        }
    }
}