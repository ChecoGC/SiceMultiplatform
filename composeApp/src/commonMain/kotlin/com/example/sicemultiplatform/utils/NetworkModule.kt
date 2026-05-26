package com.example.sicemultiplatform.utils

import io.ktor.client.*

object NetworkModule {
    var cookieSesion: String = ""

    val httpClient = HttpClient {}

    const val BASE_URL = "https://sicenet.surguanajuato.tecnm.mx/ws/wsalumnos.asmx"
}