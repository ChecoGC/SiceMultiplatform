package com.example.sicemultiplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sicemultiplatform.utils.AndroidDatabaseDriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Aquí usamos la fábrica nativa de Android, pasándole el contexto
            App(driverFactory = AndroidDatabaseDriverFactory(context = this@MainActivity))
        }
    }
}