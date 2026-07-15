package com.example.sistemacitasmedicas.model

data class CitaMedica(
    val id: Int = 0,
    val paciente_id: Int = 1,
    val paciente: String = "",
    val medico_id: Int = 1,
    val medico: String = "",
    val fecha: String,
    val hora: String,
    val motivo: String,
    val estado: String = "Programada",
    val observaciones: String = "")

