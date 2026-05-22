package com.example.sicemultiplatform

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sicemultiplatform.ui.screens.LoginScreen
import com.example.sicemultiplatform.ui.screens.ProfileScreen

@Composable
fun App() {
    MaterialTheme {
        // Instanciamos el ViewModel único universal
        val loginViewModel = viewModel { LoginViewModel() }

        // Sistema de navegación por estado reactivo
        if (!loginViewModel.isLoggedIn) {
            LoginScreen(viewModel = loginViewModel)
        } else {
            ProfileScreen(viewModel = loginViewModel)
        }
    }
}