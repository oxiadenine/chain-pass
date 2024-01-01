package io.sunland.chainpass.android

import android.os.Bundle
import android.os.Environment
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import io.sunland.chainpass.common.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsManager = SettingsManager(applicationContext.getExternalFilesDir("")!!.absolutePath)
        val database = DatabaseFactory.createDatabase(DriverFactory(applicationContext))
        val storage = Storage(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path)

        setContent {
            title = "Chain Pass"

            val themeState = rememberThemeState(ThemeMode.DARK)

            MaterialTheme(colorScheme = if (themeState.isDarkMode) {
                Theme.DarkColors
            } else Theme.LightColors) { App(settingsManager, database, storage, themeState) }
        }
    }
}