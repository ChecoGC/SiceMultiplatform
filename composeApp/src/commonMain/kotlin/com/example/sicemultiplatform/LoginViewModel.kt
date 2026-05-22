package com.example.sicemultiplatform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var offlineMessage by mutableStateOf("")
    var currentSection by mutableStateOf("PERFIL")
    var currentMatricula by mutableStateOf("")
    var profileData by mutableStateOf("")

    // Control de navegación básico para KMP
    var isLoggedIn by mutableStateOf(false)

    /**
     * Función que se ejecuta al presionar "Entrar" en el LoginScreen
     */
    fun loginAndSyncData(matricula: String, password: String) {
        if (matricula.isEmpty() || password.isEmpty()) {
            errorMessage = "Por favor, llena todos los campos."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                // Aquí se conectará Ktor en el futuro.
                // Por ahora simulamos una respuesta exitosa del servidor SICE.
                currentMatricula = matricula

                // Cargamos el perfil inicial simulado
                profileData = """{
                    "nombre": "ALONSO GUZMAN RUIZ",
                    "matricula": "$matricula",
                    "carrera": "INGENIERÍA EN SISTEMAS COMPUTACIONALES",
                    "especialidad": "TECNOLOGÍAS PARA LA INDUSTRIA 4.0",
                    "semActual": "8",
                    "estatus": "ACTIVO",
                    "cdtosAcumulados": "213"
                }"""

                isLoggedIn = true // Le da el acceso a la app
                currentSection = "PERFIL"
            } catch (e: Exception) {
                errorMessage = "Error de autenticación: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Función que se ejecuta al cambiar de sección en el Menú Lateral de ProfileScreen
     */
    fun cargarInformacion(matricula: String, seccion: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                // Simulamos las respuestas en crudo de las peticiones para alimentar tus parsers
                profileData = when (seccion) {
                    "PERFIL" -> """{
                        "nombre": "ALONSO GUZMAN RUIZ",
                        "matricula": "$matricula",
                        "carrera": "INGENIERÍA EN SISTEMAS COMPUTACIONALES",
                        "especialidad": "TECNOLOGÍAS PARA LA INDUSTRIA 4.0",
                        "semActual": "8",
                        "estatus": "ACTIVO",
                        "cdtosAcumulados": "213"
                    }"""

                    "CARGA" -> """[
                        {"Materia": "PROGRAMACION WEB III", "Docente": "Ing. Gustavo Ivan Vega", "Grupo": "A", "Creditos": "5"},
                        {"Materia": "TALLER DE INVESTIGACION II", "Docente": "Dra. Maria Lopez", "Grupo": "B", "Creditos": "4"},
                        {"Materia": "INTERNET DE LAS COSAS", "Docente": "Dr. Alejandro Silva", "Grupo": "A", "Creditos": "5"},
                        {"Materia": "SIMULACION", "Docente": "Ing. Juana Martínez", "Grupo": "C", "Creditos": "5"},
                        {"Materia": "PROGRAMACION MOVIL II", "Docente": "Mtro. Gustavo Ivan Vega", "Grupo": "A", "Creditos": "5"}
                    ]"""

                    "KARDEX" -> """[
                        {"Materia": "TALLER DE ETICA", "Calif": "82", "S1": "1", "Cdts": "4", "Acred": "Ordinario"},
                        {"Materia": "TALLER DE ADMON", "Calif": "86", "S1": "1", "Cdts": "4", "Acred": "Regularización"},
                        {"Materia": "FUND.INVESTIGAC", "Calif": "82", "S1": "1", "Cdts": "4", "Acred": "Ordinario"},
                        {"Materia": "ACT. COMPLEM.I", "Calif": "100", "S1": "1", "Cdts": "1", "Acred": "Ordinario"}
                    ]"""

                    "CALIF_UNI" -> """[
                        {"Materia": "PROGRAMACION MOVIL II", "C1": "85", "C2": "92", "C3": "88", "C4": "90"},
                        {"Materia": "SIMULACION", "C1": "74", "C2": "80", "C3": "85", "C4": "60"}
                    ]"""

                    "CALIF_FINAL" -> """[
                        {"Materia": "PROGRAMACION MOVIL II", "CalifFinal": "89", "Observaciones": "AC"},
                        {"Materia": "SIMULACION", "CalifFinal": "75", "Observaciones": "AC"},
                        {"Materia": "INTERNET DE LAS COSAS", "CalifFinal": "90", "Observaciones": "AC"}
                    ]"""

                    else -> ""
                }
            } catch (e: Exception) {
                errorMessage = "Error al recuperar datos locales: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}