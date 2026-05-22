// 1. CAMBIO AQUÍ: El nuevo paquete de tu proyecto
package com.example.sicemultiplatform.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// 2. CAMBIOS AQUÍ: Actualizamos las rutas de tu ViewModel y tus Utilerías
import com.example.sicemultiplatform.LoginViewModel
import com.example.sicemultiplatform.utils.XmlParser
import com.example.sicemultiplatform.utils.parsearCargaAcademica
import com.example.sicemultiplatform.utils.parsearPerfilAcademico
import com.example.sicemultiplatform.utils.parsearKardex
import com.example.sicemultiplatform.utils.parsearCalifUnidades
import com.example.sicemultiplatform.utils.parsearCalificacionFinal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: LoginViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menú SICE", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Perfil Académico") },
                    selected = viewModel.currentSection == "PERFIL",
                    onClick = {
                        viewModel.currentSection = "PERFIL"
                        viewModel.cargarInformacion(viewModel.currentMatricula, "PERFIL")
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Carga Académica") },
                    selected = viewModel.currentSection == "CARGA",
                    onClick = {
                        viewModel.currentSection = "CARGA"
                        viewModel.cargarInformacion(viewModel.currentMatricula, "CARGA")
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Kárdex") },
                    selected = viewModel.currentSection == "KARDEX",
                    onClick = {
                        viewModel.currentSection = "KARDEX"
                        viewModel.cargarInformacion(viewModel.currentMatricula, "KARDEX")
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Calificaciones por Unidad") },
                    selected = viewModel.currentSection == "CALIF_UNI",
                    onClick = {
                        viewModel.currentSection = "CALIF_UNI"
                        viewModel.cargarInformacion(viewModel.currentMatricula, "CALIF_UNI")
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Calificación Final") },
                    selected = viewModel.currentSection == "CALIF_FINAL",
                    onClick = {
                        viewModel.currentSection = "CALIF_FINAL"
                        viewModel.cargarInformacion(viewModel.currentMatricula, "CALIF_FINAL")
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("SICE net") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Abrir Menú")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                DataContentView(viewModel)
            }
        }
    }
}

@Composable
fun DataContentView(viewModel: LoginViewModel) {
    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF6CBF2A))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Sincronizando datos...", color = Color.Gray)
            }
        }
        return
    }
    val tagABuscar = when (viewModel.currentSection) {
        "PERFIL" -> "getAlumnoAcademicoWithLineamientoResult"
        "CARGA" -> "getCargaAcademicaByAlumnoResult"
        "KARDEX" -> "getAllKardexConPromedioByAlumnoResult"
        "CALIF_UNI" -> "getCalifUnidadesByAlumnoResult"
        "CALIF_FINAL" -> "getAllCalifFinalByAlumnosResult"
        else -> ""
    }

    val contenidoLimpio = remember(viewModel.profileData, viewModel.currentSection) {
        if (viewModel.profileData.contains("<?xml") || viewModel.profileData.contains("<soap:")) {
            XmlParser.extraerContenidoXml(viewModel.profileData, tagABuscar)
        } else {
            viewModel.profileData
        }
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {

        if (viewModel.errorMessage.isNotEmpty()) {
            Text(
                text = viewModel.errorMessage,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (viewModel.offlineMessage.isNotEmpty()) {
            Text(
                text = viewModel.offlineMessage,
                color = Color(0xFFE65100),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        when (viewModel.currentSection) {
            "PERFIL" -> PerfilAcademicoView(jsonString = contenidoLimpio)
            "CARGA" -> CargaAcademicaView(jsonString = contenidoLimpio)
            "KARDEX" -> KardexView(jsonString = contenidoLimpio)
            "CALIF_UNI" -> CalifUnidadesView(jsonString = contenidoLimpio)
            "CALIF_FINAL" -> CalificacionFinalView(jsonString = contenidoLimpio)
            else -> {
                Text(
                    text = contenidoLimpio,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

@Composable
fun CargaAcademicaView(jsonString: String) {
    val materias = remember(jsonString) { parsearCargaAcademica(jsonString) }

    if (materias.isEmpty()) {
        Text("No se pudo estructurar la carga.\n\nDatos:\n$jsonString")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(materias) { materia ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = materia.materia,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B396A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Docente: ${materia.docente}", style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Grupo: ${materia.grupo}", fontWeight = FontWeight.SemiBold)
                            Text(text = "Créditos: ${materia.creditos}", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerfilAcademicoView(jsonString: String) {
    val perfil = remember(jsonString) { parsearPerfilAcademico(jsonString) }

    if (perfil == null) {
        Text("No se pudo estructurar el perfil.\n\nDatos:\n$jsonString")
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFE0E0E0), CircleShape)
                        .padding(16.dp),
                    tint = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = perfil.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF1B396A)
                )
                Text(
                    text = perfil.matricula,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                InfoRow(label = "Carrera", value = perfil.carrera)
                InfoRow(label = "Especialidad", value = perfil.especialidad)
                InfoRow(label = "Semestre Actual", value = perfil.semActual)
                InfoRow(label = "Créditos Acum.", value = perfil.cdtosAcumulados)
                InfoRow(label = "Estatus", value = perfil.estatus)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
        Text(
            text = value,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        )
    }
}

@Composable
fun KardexView(jsonString: String) {
    val materias = remember(jsonString) { parsearKardex(jsonString) }
    val materiasPorSemestre = remember(materias) { materias.groupBy { it.semestre } }

    if (materias.isEmpty()) {
        Text("No se pudo estructurar el Kárdex.\n\nDatos:\n$jsonString")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            materiasPorSemestre.toSortedMap(compareBy { it.toIntOrNull() ?: 0 }).forEach { (semestre, listaMaterias) ->
                item {
                    Text(
                        text = "Semestre $semestre",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6CBF2A),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(listaMaterias) { materia ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = materia.materia,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B396A)
                                )
                                Text(
                                    text = "Créditos: ${materia.creditos} | ${materia.tipoEvaluacion}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = materia.calificacion,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if ((materia.calificacion.toIntOrNull() ?: 0) >= 70 || materia.calificacion == "AC") Color(0xFF6CBF2A) else Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalifUnidadesView(jsonString: String) {
    val materias = remember(jsonString) { parsearCalifUnidades(jsonString) }

    if (materias.isEmpty()) {
        Text("No se pudo estructurar las calificaciones.\n\nDatos:\n$jsonString")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(materias) { materia ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = materia.materia,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B396A)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (materia.unidades.isEmpty()) {
                                Text("Aún no hay calificaciones capturadas.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            } else {
                                materia.unidades.forEachIndexed { index, calif ->
                                    UnidadBox(unidad = index + 1, calificacion = calif)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnidadBox(unidad: Int, calificacion: String) {
    val isAprobado = (calificacion.toIntOrNull() ?: 0) >= 70

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(if (isAprobado) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = "U$unidad", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = calificacion,
            fontWeight = FontWeight.Bold,
            color = if (isAprobado) Color(0xFF2E7D32) else Color.Red
        )
    }
}

@Composable
fun CalificacionFinalView(jsonString: String) {
    val materias = remember(jsonString) { parsearCalificacionFinal(jsonString) }

    if (materias.isEmpty()) {
        Text("No se pudieron estructurar las calificaciones finales.\n\nDatos:\n$jsonString")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(materias) { materia ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = materia.materia,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B396A)
                            )
                            if (materia.observaciones.isNotEmpty()) {
                                Text(
                                    text = materia.observaciones,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }

                        val califInt = materia.calificacion.toIntOrNull() ?: 0
                        val isAprobado = califInt >= 70 || materia.calificacion == "AC"

                        Text(
                            text = materia.calificacion,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isAprobado) Color(0xFF6CBF2A) else Color.Red
                        )
                    }
                }
            }
        }
    }
}