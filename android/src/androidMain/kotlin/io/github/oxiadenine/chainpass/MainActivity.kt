package io.github.oxiadenine.chainpass

import android.os.Bundle
import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import io.github.oxiadenine.chainpass.network.SyncServer
import io.github.oxiadenine.chainpass.network.TcpSocket
import io.github.oxiadenine.chainpass.repository.ChainLinkRepository
import io.github.oxiadenine.chainpass.repository.ChainRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appDataDir = if (BuildConfig.DEBUG) {
            File("${applicationContext.getExternalFilesDir("")!!.absolutePath}/local")
        } else File(applicationContext.getExternalFilesDir("")!!.absolutePath)

        val appStorageDir = File("${Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        ).path}/Chain Pass")

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

        val syncServer = SyncServer(chainRepository, chainLinkRepository)

        CoroutineScope(Dispatchers.IO).launch {
            TcpSocket.hostAddressFlow.collectLatest { hostAddress ->
                syncServer.stop()

                try {
                    if (hostAddress.isNotEmpty()) syncServer.start(hostAddress)
                } catch (_: Exception) {}
            }
        }

        setContent {
            title = "Chain Pass"

            val settingsState = rememberSettingsState(settings)
            val networkState = rememberNetworkState(TcpSocket.hostAddressFlow)
            val themeState = rememberThemeState(ThemeMode.DARK)
            val navHostController = rememberNavController()

            BackHandler(
                enabled = true,
                onBack = {
                    if (!navHostController.popBackStack()) finish()
                }
            )

            MaterialTheme(colorScheme = if (themeState.isDarkMode) {
                Theme.DarkColors
            } else Theme.LightColors) {
                App(
                    chainRepository = chainRepository,
                    chainLinkRepository = chainLinkRepository,
                    settingsState = settingsState,
                    networkState = networkState,
                    themeState = themeState,
                    navHostController = navHostController
                )
            }
        }
    }
}