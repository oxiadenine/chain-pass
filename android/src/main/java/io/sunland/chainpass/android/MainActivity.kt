package io.sunland.chainpass.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.sunland.chainpass.common.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsManager = SettingsManager(applicationContext.getExternalFilesDir("")!!.absolutePath)
        val database = DatabaseFactory.createDatabase(DriverFactory(applicationContext))
        val storage = Storage(settingsManager.dirPath)

        setContent {
            title = "Chain Pass"

            App(settingsManager, database, storage)
        }
    }
}