package com.example.sistemacitasmedicas.view

import com.example.sistemacitasmedicas.model.CitaMedica

sealed class CitaUiState {
    object Cargando : CitaUiState()
    data class Exito(val citas: List<CitaMedica>) : CitaUiState()
    data class Error(val mensaje: String) : CitaUiState()
}