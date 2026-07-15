package com.example.sistemacitasmedicas.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sistemacitasmedicas.model.CitaMedica
import com.example.sistemacitasmedicas.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class CitasViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CitaUiState>(CitaUiState.Cargando)
    val uiState: StateFlow<CitaUiState> = _uiState.asStateFlow()

    // Inicialización explícita y segura de inputs
    var pacienteInput by mutableStateOf("")
    var medicoInput by mutableStateOf("")
    var fechaInput by mutableStateOf("")
    var horaInput by mutableStateOf("")
    var motivoInput by mutableStateOf("")
    var estadoInput by mutableStateOf("Programada")
    var observacionesInput by mutableStateOf("")

    var citaSeleccionadaId by mutableStateOf<Int?>(null)

    init {
        obtenerCitas()
    }

    fun obtenerCitas() {
        viewModelScope.launch {
            _uiState.value = CitaUiState.Cargando
            try {
                val lista = RetrofitClient.apiService.obtenerCitas()
                _uiState.value = CitaUiState.Exito(lista)
            } catch (e: IOException) {
                _uiState.value = CitaUiState.Error("No se pudo conectar al servidor.")
            } catch (e: Exception) {
                _uiState.value = CitaUiState.Error("Error al convertir datos o fallo en el servidor.")
            }
        }
    }

    fun guardarCita(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (fechaInput.isBlank() || horaInput.isBlank() || motivoInput.isBlank()) {
            onError("Los campos obligatorios (*) no pueden estar vacíos.")
            return
        }

        val citaData = CitaMedica(
            id = citaSeleccionadaId ?: 0,
            paciente_id = 1,
            paciente = pacienteInput,
            medico_id = 1,
            medico = medicoInput,
            fecha = fechaInput,
            hora = horaInput,
            motivo = motivoInput,
            estado = estadoInput,
            observaciones = observacionesInput
        )

        viewModelScope.launch {
            try {
                if (citaSeleccionadaId == null) {
                    RetrofitClient.apiService.agregarCita(citaData)
                } else {
                    RetrofitClient.apiService.actualizarCita(citaSeleccionadaId!!, citaData)
                }
                limpiarFormulario()
                obtenerCitas()
                onSuccess()
            } catch (e: Exception) {
                onError("Error al guardar: ${e.localizedMessage}")
            }
        }
    }

    fun eliminarCita(id: Int, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.eliminarCita(id)
                obtenerCitas()
            } catch (e: Exception) {
                onError("No se pudo eliminar: ${e.localizedMessage}")
            }
        }
    }

    fun cargarCitaParaEdicion(cita: CitaMedica) {
        citaSeleccionadaId = cita.id
        pacienteInput = cita.paciente.ifBlank { "" }
        medicoInput = cita.medico.ifBlank { "" }
        fechaInput = cita.fecha
        horaInput = cita.hora
        motivoInput = cita.motivo
        estadoInput = cita.estado
        observacionesInput = cita.observaciones.ifBlank { "" }
    }

    fun limpiarFormulario() {
        citaSeleccionadaId = null
        pacienteInput = ""
        medicoInput = ""
        fechaInput = ""
        horaInput = ""
        motivoInput = ""
        estadoInput = "Programada"
        observacionesInput = ""
    }


    var listaMedicosApi by mutableStateOf<List<com.example.sistemacitasmedicas.model.Medico>>(emptyList())


    fun obtenerMedicosDeBaseDatos() {
        viewModelScope.launch {
            try {
                val medicos = RetrofitClient.apiService.obtenerMedicos()
                listaMedicosApi = medicos
            } catch (e: Exception) {
            }
        }
    }

    init {
        obtenerCitas()
        obtenerMedicosDeBaseDatos()
    }
}