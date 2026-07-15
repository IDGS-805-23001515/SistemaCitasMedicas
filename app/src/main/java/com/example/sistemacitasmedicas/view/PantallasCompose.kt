package com.example.sistemacitasmedicas.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sistemacitasmedicas.model.CitaMedica
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(
    viewModel: CitasViewModel,
    onNavegarAlta: () -> Unit,
    onNavegarEdicion: (CitaMedica) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var citaParaEliminar by remember { mutableStateOf<CitaMedica?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clínica - Citas Médicas", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.obtenerCitas() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavegarAlta,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Cita")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            when (val estado = uiState) {
                is CitaUiState.Cargando -> CircularProgressIndicator()
                is CitaUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(estado.mensaje, color = Color.Red, modifier = Modifier.padding(16.dp))
                        Button(onClick = { viewModel.obtenerCitas() }) { Text("Reintentar") }
                    }
                }
                is CitaUiState.Exito -> {
                    if (estado.citas.isEmpty()) {
                        Text("No hay citas registradas en el sistema.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(estado.citas) { index, cita ->
                                var visible by remember { mutableStateOf(false) }
                                LaunchedEffect(key1 = true) { visible = true }

                                AnimatedVisibility(
                                    visible = visible,
                                    enter = fadeIn(animationSpec = tween(400, index * 50)) +
                                            slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(400, index * 50))
                                ) {
                                    ElementoCita(
                                        cita = cita,
                                        onEditar = { onNavegarEdicion(cita) },
                                        onEliminar = {
                                            citaParaEliminar = cita
                                            mostrarDialogoEliminar = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoEliminar && citaParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Cancelar Cita") },
            text = { Text("¿Deseas eliminar permanentemente esta cita?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarCita(citaParaEliminar!!.id, onError = {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    })
                    mostrarDialogoEliminar = false
                }) { Text("Confirmar", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoEliminar = false }) { Text("Volver") } }
        )
    }
}

@Composable
fun ElementoCita(cita: CitaMedica, onEditar: () -> Unit, onEliminar: () -> Unit) {
    val colorEstado = when (cita.estado.lowercase()) {
        "confirmada" -> Color(0xFF4CAF50)
        "cancelada" -> Color(0xFFF44336)
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.fillMaxHeight().width(6.dp).background(colorEstado))

            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Paciente: ${cita.paciente}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Médico: ${cita.medico}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Motivo: ${cita.motivo}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "📅 ${cita.fecha}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        Text(text = "⏰ ${cita.hora}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = onEliminar) { Icon(Icons.Default.Delete, "Borrar", tint = Color(0xFFD32F2F)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAlta(viewModel: CitasViewModel, onRegresar: () -> Unit) {
    val context = LocalContext.current
    Scaffold(topBar = { TopAppBar(title = { Text("Agendar Cita Nueva") }, navigationIcon = { IconButton(onClick = onRegresar) { Icon(Icons.Default.ArrowBack, "") } }) }) {
        FormularioCita(viewModel, Modifier.padding(it), "Registrar", onRegresar, context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEdicion(viewModel: CitasViewModel, onRegresar: () -> Unit) {
    val context = LocalContext.current
    Scaffold(topBar = { TopAppBar(title = { Text("Modificar Registro") }, navigationIcon = { IconButton(onClick = onRegresar) { Icon(Icons.Default.ArrowBack, "") } }) }) {
        FormularioCita(viewModel, Modifier.padding(it), "Actualizar Cita", onRegresar, context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioCita(
    viewModel: CitasViewModel,
    modifier: Modifier,
    accionTexto: String,
    onRegresar: () -> Unit,
    context: android.content.Context
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    val calendario = Calendar.getInstance()

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, año, mes, dia ->
                val mesFormateado = String.format("%02d", mes + 1)
                val diaFormateado = String.format("%02d", dia)
                viewModel.fechaInput = "$año-$mesFormateado-$diaFormateado"
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hora, minuto ->
                val horaFormateada = String.format("%02d", hora)
                val minutoFormateado = String.format("%02d", minuto)
                viewModel.horaInput = "$horaFormateada:$minutoFormateado"
            },
            calendario.get(Calendar.HOUR_OF_DAY),
            calendario.get(Calendar.MINUTE),
            true
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding() // Empuja el formulario hacia arriba cuando se abre el teclado
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = viewModel.pacienteInput,
            onValueChange = { if (it.length <= 50) viewModel.pacienteInput = it },
            label = { Text("Nombre del Paciente * (Máx 50)") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = expandedDropdown,
            onExpandedChange = { expandedDropdown = !expandedDropdown }
        ) {
            OutlinedTextField(
                value = viewModel.medicoInput.ifBlank { "Selecciona un médico..." },
                onValueChange = {},
                readOnly = true,
                label = { Text("Médico Asignado *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false }
            ) {
                if (viewModel.listaMedicosApi.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Cargando médicos de la base de datos...") },
                        onClick = { }
                    )
                } else {
                    viewModel.listaMedicosApi.forEach { doc ->
                        DropdownMenuItem(
                            text = { Text("${doc.nombre_completo} (${doc.especialidad_nombre})") },
                            onClick = {
                                // Guardamos el formato final que incluye la especialidad médica
                                viewModel.medicoInput = "${doc.nombre_completo} (${doc.especialidad_nombre})"
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = viewModel.fechaInput,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha de Cita *") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Fecha", tint = MaterialTheme.colorScheme.primary)
                }
            }
        )

        OutlinedTextField(
            value = viewModel.horaInput,
            onValueChange = {},
            readOnly = true,
            label = { Text("Hora de Cita *") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { timePickerDialog.show() }) {
                    Icon(Icons.Default.Build, contentDescription = "Hora", tint = MaterialTheme.colorScheme.primary)
                }
            }
        )

        OutlinedTextField(
            value = viewModel.motivoInput,
            onValueChange = { if (it.length <= 150) viewModel.motivoInput = it },
            label = { Text("Motivo de Consulta * (Máx 150)") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.estadoInput,
            onValueChange = { if (it.length <= 20) viewModel.estadoInput = it },
            label = { Text("Estado (Programada/Confirmada)") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.observacionesInput,
            onValueChange = { if (it.length <= 200) viewModel.observacionesInput = it },
            label = { Text("Observaciones (Máx 200)") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (viewModel.pacienteInput.isBlank() || viewModel.medicoInput.isBlank() || viewModel.fechaInput.isBlank() || viewModel.horaInput.isBlank()) {
                    Toast.makeText(context, "Por favor llena todos los campos obligatorios.", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.guardarCita(
                        onSuccess = {
                            Toast.makeText(context, "Operación exitosa", Toast.LENGTH_SHORT).show()
                            onRegresar()
                        },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) { Text(accionTexto, fontWeight = FontWeight.Bold) }
    }
}