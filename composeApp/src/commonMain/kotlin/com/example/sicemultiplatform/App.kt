package com.example.sicemultiplatform

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sicemultiplatform.ui.screens.LoginScreen
import com.example.sicemultiplatform.ui.screens.ProfileScreen
import com.example.sicemultiplatform.utils.DatabaseDriverFactory
import com.example.sicemultiplatform.database.AppDatabase

@Composable
fun App(driverFactory: DatabaseDriverFactory) { // <-- Ahora recibe la fábrica
    MaterialTheme {
        // Construimos la base de datos universal
        val database = AppDatabase(driverFactory.createDriver())

        // Le pasamos la base de datos al ViewModel
        val loginViewModel = viewModel { LoginViewModel(database) }

        if (!loginViewModel.isLoggedIn) {
            LoginScreen(viewModel = loginViewModel)
        } else {
            ProfileScreen(viewModel = loginViewModel)
        }
    }
}