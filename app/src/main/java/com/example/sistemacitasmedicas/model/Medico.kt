package com.example.sistemacitasmedicas.model

data class Medico(
    val id: Int,
    val nombre_completo: String,
    val especialidad_id: Int,
    val especialidad_nombre: String,
    val cedula_profesional: String,
    val telefono: String?,
    val correo: String?,
    val horario_atencion: String?,
    val estado: String
)