package io.github.oxiadenine.chainpass

import android.os.Bundle
import android.os.Environment
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
    private var syncServer: SyncServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

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

        syncServer = SyncServer(chainRepository, chainLinkRepository)

        CoroutineScope(Dispatchers.IO).launch {
            TcpSocket.hostAddressFlow.collectLatest { hostAddress ->
                syncServer?.stop()

                try {
                    if (hostAddress.isNotEmpty()) {
                        syncServer?.start(hostAddress)
                    }
                } catch (e: Throwable) {
                    if (BuildConfig.DEBUG) println(e)
                }
            }
        }

        setContent {
            title = "Chain Pass"

            val networkState = rememberNetworkState(TcpSocket.hostAddressFlow)
            val themeState = rememberThemeState(ThemeMode.DARK)
            val navHostController = rememberNavController()

            val configuration = LocalConfiguration.current
            val density = LocalDensity.current

            val windowInsets = WindowInsets.safeContent

            val screen = with(density) {
                val insetsLeft = windowInsets.getLeft(this, LayoutDirection.Ltr).toDp()
                val insetsRight = windowInsets.getRight(this, LayoutDirection.Ltr).toDp()
                val insetsTop = windowInsets.getTop(this).toDp()
                val insetsBottom = windowInsets.getBottom(this).toDp()

                Screen(
                    width = configuration.screenWidthDp.dp - insetsLeft - insetsRight,
                    height = configuration.screenHeightDp.dp - insetsTop - insetsBottom
                )
            }

            CompositionLocalProvider(LocalScreen provides screen) {
                MaterialTheme(colorScheme = if (themeState.isDarkMode) {
                    Theme.DarkColors
                } else Theme.LightColors) {
                    BackHandler {
                        if (!navHostController.popBackStack()) {
                            syncServer?.stop()

                            finish()
                        }
                    }

                    App(chainRepository, chainLinkRepository, settings, networkState, themeState, navHostController)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        syncServer?.stop()
    }
}