package com.example.sicemultiplatform// Archivo: shared/src/desktopMain/kotlin/Main.kt

import DesktopDatabaseDriverFactory
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.sicemultiplatform.utils.DatabaseDriverFactory

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "SICE net") {
        App(driverFactory = DesktopDatabaseDriverFactory())
    }
}