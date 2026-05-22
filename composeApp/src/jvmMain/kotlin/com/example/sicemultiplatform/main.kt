package com.example.sicemultiplatform// Archivo: shared/src/desktopMain/kotlin/Main.kt

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    // Definimos cómo se ve la ventana en Windows/macOS
    Window(
        onCloseRequest = ::exitApplication,
        title = "SICE net - Desktop Client"
    ) {
        // Mandamos llamar exactamente a la misma UI universal
        App()
    }
}