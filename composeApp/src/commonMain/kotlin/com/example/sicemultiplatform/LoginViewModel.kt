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
                    header("Cookie", "AspxAutoDetectCookieSupport=1")
                    setBody(soapRequest)
                }

                val responseBody = response.bodyAsText()

                // --- VAMOS A VER QUÉ NOS CONTESTA EL LOGIN ---
                println("====== RESPUESTA CRUDA DEL LOGIN ======")
                println(responseBody)

                var sessionCookies = ""
                response.setCookie().forEach { cookie ->
                    sessionCookies += "${cookie.name}=${cookie.value}; "
                }
                NetworkModule.cookieSesion = sessionCookies

                println("=== COOKIE GUARDADA: ${NetworkModule.cookieSesion} ===")
                println("=======================================")

                // 3. LEEMOS LA RESPUESTA
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

            }catch (e: Exception) {

                val datosOffline = dbQueries.getAlumnoData(currentMatricula, currentSection).executeAsOneOrNull()

                if (datosOffline != null) {
                    offlineMessage = "Estás navegando en Modo Offline"
                    profileData = datosOffline.xmlData // Le pasamos el XML viejo a la pantalla
                } else {
                    errorMessage = "Error de red y no hay datos guardados: ${e.message}"
                }
            }finally {
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
                // 1. Limpiamos la pantalla para que no se mezcle información
                profileData = ""

                val soapActionName = when (seccion) {
                    "PERFIL" -> "getAlumnoAcademicoWithLineamiento"
                    "CARGA" -> "getCargaAcademicaByAlumno"
                    "KARDEX" -> "getAllKardexConPromedioByAlumno"
                    "CALIF_UNI" -> "getCalifUnidadesByAlumno"
                    "CALIF_FINAL" -> "getAllCalifFinalByAlumnos"
                    else -> throw Exception("Sección desconocida")
                }

                // 2. XML COMPLETAMENTE PLANO
                // Sin saltos de línea ni espacios que confundan al servidor antiguo
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

                    // Aquí llamamos a la función exacta que vimos en el archivo generado
                    dbQueries.insertAlumno(
                        matricula = currentMatricula,
                        seccion = seccion,
                        xmlData = responseBody
                    )
                }

            } catch (e: Exception) {
                // Aquí usamos la consulta tal cual la define el archivo generado
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