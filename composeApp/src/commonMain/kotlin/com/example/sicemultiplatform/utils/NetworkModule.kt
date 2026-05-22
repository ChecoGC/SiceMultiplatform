package com.example.sicemultiplatform.utils

import io.ktor.client.*

object NetworkModule {
    // Aquí guardaremos la cookie real de sesión del SICE
    var cookieSesion: String = ""

    // Ya no necesitamos el plugin automático, usaremos un cliente limpio
    val httpClient = HttpClient {}

    const val BASE_URL = "https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx"
}