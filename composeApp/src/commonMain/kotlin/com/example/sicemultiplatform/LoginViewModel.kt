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
import com.example.sicemultiplatform.database.AppDatabase

class LoginViewModel(private val database: AppDatabase) : ViewModel() {

    private val dbQueries = database.alumnoQueries
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var offlineMessage by mutableStateOf("")
    var currentSection by mutableStateOf("PERFIL")
    var currentMatricula by mutableStateOf("")
    var profileData by mutableStateOf("")


    var isLoggedIn by mutableStateOf(false)


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


                val response = NetworkModule.httpClient.post(NetworkModule.BASE_URL) {
                    header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                    header("SOAPAction", "http://tempuri.org/accesoLogin")
                    header("Cookie", "AspxAutoDetectCookieSupport=1")
                    setBody(soapRequest)
                }

                val responseBody = response.bodyAsText()


                println("====== RESPUESTA CRUDA DEL LOGIN ======")
                println(responseBody)

                var sessionCookies = ""
                response.setCookie().forEach { cookie ->
                    sessionCookies += "${cookie.name}=${cookie.value}; "
                }
                NetworkModule.cookieSesion = sessionCookies

                println("=== COOKIE GUARDADA: ${NetworkModule.cookieSesion} ===")
                println("=======================================")


                val resultadoLogin = XmlParser.extraerContenidoXml(responseBody, "accesoLoginResult")



                if (resultadoLogin.contains("false", ignoreCase = true) || resultadoLogin.contains("error", ignoreCase = true)) {
                    errorMessage = "Credenciales incorrectas o error en el servidor."
                } else {

                    isLoggedIn = true
                    currentSection = "PERFIL"

                    cargarInformacion(matricula, "PERFIL")
                }

            }catch (e: Exception) {

                val datosOffline = dbQueries.getAlumnoData(currentMatricula, currentSection).executeAsOneOrNull()

                if (datosOffline != null) {
                    offlineMessage = "Estás navegando en Modo Offline"
                    profileData = datosOffline.xmlData
                } else {
                    errorMessage = "Error de red y no hay datos guardados: ${e.message}"
                }
            }finally {
                isLoading = false
            }
        }
    }


    fun cargarInformacion(matricula: String, seccion: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {

                profileData = ""

                val soapActionName = when (seccion) {
                    "PERFIL" -> "getAlumnoAcademicoWithLineamiento"
                    "CARGA" -> "getCargaAcademicaByAlumno"
                    "KARDEX" -> "getAllKardexConPromedioByAlumno"
                    "CALIF_UNI" -> "getCalifUnidadesByAlumno"
                    "CALIF_FINAL" -> "getAllCalifFinalByAlumnos"
                    else -> throw Exception("Sección desconocida")
                }


                val bodyContent = when (seccion) {
                    "PERFIL" -> "<getAlumnoAcademicoWithLineamiento xmlns=\"http://tempuri.org/\" />"
                    "CARGA" -> "<getCargaAcademicaByAlumno xmlns=\"http://tempuri.org/\" />"
                    "KARDEX" -> "<getAllKardexConPromedioByAlumno xmlns=\"http://tempuri.org/\"><aluLineamiento>1</aluLineamiento></getAllKardexConPromedioByAlumno>"
                    "CALIF_UNI" -> "<getCalifUnidadesByAlumno xmlns=\"http://tempuri.org/\" />"
                    "CALIF_FINAL" -> "<getAllCalifFinalByAlumnos xmlns=\"http://tempuri.org/\"><bytModEducativo>1</bytModEducativo></getAllCalifFinalByAlumnos>"
                    else -> ""
                }

                val soapRequest = "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>$bodyContent</soap:Body></soap:Envelope>"

                println("--- DEBUG: Enviando Cookie: ${NetworkModule.cookieSesion} ---")

                val response = NetworkModule.httpClient.post(NetworkModule.BASE_URL) {
                    header(HttpHeaders.ContentType, "text/xml; charset=utf-8")
                    header("SOAPAction", "\"http://tempuri.org/$soapActionName\"")
                    header("Cookie", NetworkModule.cookieSesion) // <-- ¿Esta variable tiene contenido?
                    setBody(soapRequest)
                }

                val responseBody = response.bodyAsText()

                println("====== RESPUESTA CRUDA DEL SICE ($seccion) ======")
                println(responseBody)
                println("=================================================")

                if (responseBody.contains("Server was unable to process request") || responseBody.contains("Error de seguridad")) {
                    errorMessage = "Error de sesión al consultar $seccion. Vuelve a iniciar sesión."
                    isLoggedIn = false
                } else {
                    profileData = responseBody


                    dbQueries.insertAlumno(
                        matricula = currentMatricula,
                        seccion = seccion,
                        xmlData = responseBody
                    )
                }

            } catch (e: Exception) {

                val datosOffline = dbQueries.getAlumnoData(currentMatricula, seccion).executeAsOneOrNull()

                if (datosOffline != null) {
                    offlineMessage = "Estás navegando en Modo Offline"
                    profileData = datosOffline.xmlData
                } else {
                    errorMessage = "Error de red y no hay datos guardados: ${e.message}"
                }
            } finally {
                isLoading = false
            }
        }
    }
}