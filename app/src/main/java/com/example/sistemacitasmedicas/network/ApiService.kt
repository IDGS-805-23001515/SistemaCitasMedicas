package com.example.sistemacitasmedicas.network

import com.example.sistemacitasmedicas.model.CitaMedica
import com.example.sistemacitasmedicas.model.Medico
import retrofit2.http.*

interface ApiService {
    @GET("citas")
    suspend fun obtenerCitas(): List<CitaMedica>

    @POST("citas")
    suspend fun agregarCita(@Body cita: CitaMedica): Map<String, Any>

    @PUT("citas/{id}")
    suspend fun actualizarCita(@Path("id") id: Int, @Body cita: CitaMedica): Map<String, Any>

    @DELETE("citas/{id}")
    suspend fun eliminarCita(@Path("id") id: Int): Map<String, String>

    @GET("medicos")
    suspend fun obtenerMedicos(): List<Medico>
}