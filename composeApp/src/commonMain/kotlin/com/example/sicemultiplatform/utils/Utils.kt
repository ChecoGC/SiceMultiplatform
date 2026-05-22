package com.example.sicemultiplatform.utils

import kotlinx.serialization.json.*

object XmlParser {
    fun extraerContenidoXml(xml: String, tag: String): String {
        val startTag = "<$tag>"
        val endTag = "</$tag>"

        if (!xml.contains(startTag) || !xml.contains(endTag)) {
            return "Cargando datos o no se encontró la información..."
        }

        return xml.substringAfter(startTag)
            .substringBefore(endTag)
            .replace("<![CDATA[", "")
            .replace("]]>", "")
            .trim()
    }
}

fun JsonObject.optString(key: String, fallback: String = ""): String {
    val element = this[key]
    if (element == null || element is JsonNull) return fallback
    val content = element.jsonPrimitive.content
    return if (content.isEmpty() || content == "null") fallback else content
}

// =========================================================
// MODELO PARA CARGA ACADEMICA
// =========================================================
data class MateriaCarga(
    val materia: String,
    val docente: String,
    val grupo: String,
    val creditos: String
)

fun parsearCargaAcademica(jsonString: String): List<MateriaCarga> {
    val lista = mutableListOf<MateriaCarga>()
    try {
        // ESCUDO DE SEGURIDAD
        if (jsonString.contains("Cargando datos")) return lista

        val jsonArray = Json.parseToJsonElement(jsonString).jsonArray
        for (item in jsonArray) {
            val obj = item.jsonObject
            lista.add(
                MateriaCarga(
                    materia = obj.optString("materia", obj.optString("Materia", "Materia Desconocida")),
                    docente = obj.optString("docente", obj.optString("Docente", "Sin docente asignado")),
                    grupo = obj.optString("grupo", obj.optString("Grupo", "N/A")),
                    creditos = obj.optString("creditos", obj.optString("Creditos", "0"))
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return lista
}

// =========================================================
// MODELO PARA PERFIL ACADEMICO
// =========================================================
data class PerfilAcademico(
    val nombre: String,
    val matricula: String,
    val carrera: String,
    val especialidad: String,
    val semActual: String,
    val estatus: String,
    val cdtosAcumulados: String
)

fun parsearPerfilAcademico(jsonString: String): PerfilAcademico? {
    return try {
        // ESCUDO DE SEGURIDAD (Regresa null si no hay datos)
        if (jsonString.contains("Cargando datos")) return null

        val jsonObject = Json.parseToJsonElement(jsonString).jsonObject
        PerfilAcademico(
            nombre = jsonObject.optString("nombre", "Sin nombre"),
            matricula = jsonObject.optString("matricula", "N/A"),
            carrera = jsonObject.optString("carrera", "N/A"),
            especialidad = jsonObject.optString("especialidad", "N/A"),
            semActual = jsonObject.optString("semActual", "N/A"),
            estatus = jsonObject.optString("estatus", "N/A"),
            cdtosAcumulados = jsonObject.optString("cdtosAcumulados", "0")
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// =========================================================
// MODELO PARA KARDEX
// =========================================================
data class MateriaKardex(
    val materia: String,
    val calificacion: String,
    val semestre: String,
    val creditos: String,
    val tipoEvaluacion: String
)

fun parsearKardex(jsonString: String): List<MateriaKardex> {
    val lista = mutableListOf<MateriaKardex>()
    try {
        // ESCUDO DE SEGURIDAD
        if (jsonString.contains("Cargando datos")) return lista

        val startIndex = jsonString.indexOfFirst { it == '{' || it == '[' }
        val endIndex = jsonString.indexOfLast { it == '}' || it == ']' }

        if (startIndex == -1 || endIndex == -1) return lista

        val pureJson = jsonString.substring(startIndex, endIndex + 1)
        val element = Json.parseToJsonElement(pureJson)

        val jsonArray = if (element is JsonArray) {
            element
        } else {
            val jsonObject = element.jsonObject
            var arrayEncontrado: JsonArray? = null

            for (key in jsonObject.keys) {
                val posibleArreglo = jsonObject[key]
                if (posibleArreglo is JsonArray) {
                    arrayEncontrado = posibleArreglo
                    break
                }
            }
            arrayEncontrado ?: JsonArray(emptyList())
        }

        for (item in jsonArray) {
            try {
                val obj = item.jsonObject
                lista.add(
                    MateriaKardex(
                        materia = obj.optString("Materia", "Desconocida"),
                        calificacion = obj.optString("Calif", "0"),
                        semestre = obj.optString("S1", "0"),
                        creditos = obj.optString("Cdts", "0"),
                        tipoEvaluacion = obj.optString("Acred", "")
                    )
                )
            } catch (e: Exception) {
                // Se ignora materia corrupta
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return lista
}

// =========================================================
// MODELO PARA CALIFICACION POR UNIDAD
// =========================================================
data class CalificacionUnidad(
    val materia: String,
    val unidades: List<String>
)

fun parsearCalifUnidades(jsonString: String): List<CalificacionUnidad> {
    val lista = mutableListOf<CalificacionUnidad>()
    try {
        // ESCUDO DE SEGURIDAD
        if (jsonString.contains("Cargando datos")) return lista

        val jsonArray = Json.parseToJsonElement(jsonString).jsonArray

        for (item in jsonArray) {
            val obj = item.jsonObject
            val materia = obj.optString("Materia", obj.optString("materia", "Desconocida"))
            val calificaciones = mutableListOf<String>()

            for (j in 1..13) {
                val calif = obj.optString("C$j", "")
                if (calif.isNotEmpty() && calif != "null") {
                    calificaciones.add(calif)
                }
            }
            lista.add(CalificacionUnidad(materia, calificaciones))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return lista
}

// =========================================================
// MODELO FINAL
// =========================================================
data class CalificacionFinal(
    val materia: String,
    val calificacion: String,
    val observaciones: String
)

fun parsearCalificacionFinal(jsonString: String): List<CalificacionFinal> {
    val lista = mutableListOf<CalificacionFinal>()
    try {
        // ESCUDO DE SEGURIDAD
        if (jsonString.contains("Cargando datos")) return lista

        val jsonArray = Json.parseToJsonElement(jsonString).jsonArray

        for (item in jsonArray) {
            val obj = item.jsonObject
            lista.add(
                CalificacionFinal(
                    materia = obj.optString("Materia", obj.optString("materia", "Desconocida")),
                    calificacion = obj.optString("CalifFinal", obj.optString("califFinal", obj.optString("Calificacion", obj.optString("Calif", "0")))),
                    observaciones = obj.optString("Observaciones", obj.optString("observaciones", obj.optString("Acred", "")))
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return lista
}