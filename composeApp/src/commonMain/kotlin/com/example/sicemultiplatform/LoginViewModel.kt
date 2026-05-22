package com.example.sicemultiplatform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.example.sicemultiplatform.utils.NetworkModule
import com.example.sicemultiplatform.utils.XmlParser

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
                currentMatricula = matricula

                // 1. ARMAMOS EL SOBRE SOAP (El XML que se envía al servidor)
                val soapRequest = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                      <soap:Body>
                        <accesoLogin xmlns="http://tempuri.org/">
                          <strMatricula>$matricula</strMatricula>
                          <strContrasenia>$password</strContrasenia>
                          <tipoUsuario>ALUMNO</tipoUsuario>
                        </accesoLogin>
                      </soap:Body>
                    </soap:Envelope>
                """.trimIndent()

                // 2. DISPARAMOS LA PETICIÓN CON KTOR
                val response = NetworkModule.httpClient.post(NetworkModule.BASE_URL) {
                    header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                    header("SOAPAction", "http://tempuri.org/accesoLogin")
                    setBody(soapRequest)
                }

                // --- ¡NUEVO!: ATRAPAMOS LA COOKIE MANUALMENTE ---
                val rawCookies = response.headers.getAll("Set-Cookie")
                var sessionCookies = ""
                rawCookies?.forEach { cookieString ->
                    // Cortamos la basura y nos quedamos solo con la llave pura (.ASPXAUTH)
                    sessionCookies += cookieString.substringBefore(";") + "; "
                }
                NetworkModule.cookieSesion = sessionCookies
                // ------------------------------------------------

                // 3. LEEMOS LA RESPUESTA
                val responseBody = response.bodyAsText()

                // Verificamos si la respuesta XML fue exitosa ("accesoLoginResult" suele traer un boolean o un XML)
                val resultadoLogin = XmlParser.extraerContenidoXml(responseBody, "accesoLoginResult")

                // Aquí depende exactamente de qué regresa tu servidor en caso de error
                if (resultadoLogin.contains("false", ignoreCase = true) || resultadoLogin.contains("error", ignoreCase = true)) {
                    errorMessage = "Credenciales incorrectas o error en el servidor."
                } else {
                    // ¡Login Exitoso! Ktor ya guardó la Cookie en memoria automáticamente.
                    isLoggedIn = true
                    currentSection = "PERFIL"
                    // Lanzamos la carga de información real usando la cookie
                    cargarInformacion(matricula, "PERFIL")
                }

            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
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
                val soapActionName = when (seccion) {
                    "PERFIL" -> "getAlumnoAcademicoWithLineamiento"
                    "CARGA" -> "getCargaAcademicaByAlumno"
                    "KARDEX" -> "getAllKardexConPromedioByAlumno"
                    "CALIF_UNI" -> "getCalifUnidadesByAlumno"
                    "CALIF_FINAL" -> "getAllCalifFinalByAlumnos"
                    else -> throw Exception("Sección desconocida")
                }

                // El SICENET es delicado con los nombres de las variables
                val parametroXML = if (seccion == "PERFIL") {
                    "<strMatricula>$matricula</strMatricula>"
                } else {
                    "<aluControl>$matricula</aluControl>"
                }

                val soapRequest = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                      <soap:Body>
                        <$soapActionName xmlns="http://tempuri.org/">
                          $parametroXML
                        </$soapActionName>
                      </soap:Body>
                    </soap:Envelope>
                """.trimIndent()

                val response = NetworkModule.httpClient.post(NetworkModule.BASE_URL) {
                    header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                    header("SOAPAction", "http://tempuri.org/$soapActionName")
                    // --- ¡NUEVO!: INYECTAMOS LA COOKIE EN LA PETICIÓN ---
                    header("Cookie", NetworkModule.cookieSesion)
                    setBody(soapRequest)
                }

                val responseBody = response.bodyAsText()

                // (Opcional) Puedes dejar este println para confirmar que ya llegan los datos
                println("====== RESPUESTA CRUDA DEL SICE ($seccion) ======")
                println(responseBody)
                println("=================================================")

                if (responseBody.contains("Server was unable to process request") || responseBody.contains("Error de seguridad")) {
                    errorMessage = "Error de sesión al consultar $seccion. Vuelve a iniciar sesión."
                    isLoggedIn = false
                } else {
                    profileData = responseBody
                }

            } catch (e: Exception) {
                errorMessage = "Error de red al descargar $seccion: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}