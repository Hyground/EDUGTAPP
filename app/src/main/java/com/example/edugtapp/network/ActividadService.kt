package com.example.edugtapp.network

import android.util.Log
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull

object ActividadService {
    private const val BASE_URL = "https://eduapi-production.up.railway.app/api/evaluaciones"
    private val client = OkHttpClient()

    fun obtenerActividades(
        gradoId: Int,
        seccionId: Int,
        cursoId: Int,
        bimestreId: Int,
        callback: (JSONArray) -> Unit
    ) {
        val url =
            "$BASE_URL/filtrar?gradoId=$gradoId&seccionId=$seccionId&cursoId=$cursoId&bimestreId=$bimestreId"
        Log.d("ActividadService", "Llamando a URL: $url")

        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ActividadService", "Fallo de red: ${e.message}")
                callback(JSONArray())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("ActividadService", "Respuesta cruda: $body")

                try {
                    val jsonArray = JSONArray(body ?: "[]")
                    Log.d("ActividadService", "Actividades parseadas: ${jsonArray.length()}")
                    callback(jsonArray)
                } catch (e: Exception) {
                    Log.e("ActividadService", "Error al convertir JSON: ${e.message}")
                    callback(JSONArray())
                }
            }
        })
    }

    fun crearActividad(
        gradoId: Int,
        seccionId: Int,
        cursoId: Int,
        bimestreId: Int,
        nombre: String,
        tipo: String,
        ponderacion: Double,
        callback: () -> Unit
    ) {
        val json = JSONObject().apply {
            put("nombre", nombre)
            put("tipo", tipo)
            put("ponderacion", ponderacion)
            put("grado", JSONObject().put("id", gradoId))
            put("seccion", JSONObject().put("id", seccionId))
            put("curso", JSONObject().put("id", cursoId))
            put("bimestre", JSONObject().put("id", bimestreId))
        }

        val body = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder().url(BASE_URL).post(body).build()

        Log.d("ActividadService", "Enviando POST: $json")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ActividadService", "Fallo al crear actividad: ${e.message}")
                callback()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("ActividadService", "Actividad creada, código: ${response.code}")
                callback()
            }
        })
    }

    fun actualizarActividad(
        id: Int,
        gradoId: Int,
        seccionId: Int,
        cursoId: Int,
        bimestreId: Int,
        nombre: String,
        tipo: String,
        ponderacion: Double,
        callback: () -> Unit
    ) {
        val json = JSONObject().apply {
            put("id", id)
            put("nombre", nombre)
            put("tipo", tipo)
            put("ponderacion", ponderacion)
            put("grado", JSONObject().put("id", gradoId))
            put("seccion", JSONObject().put("id", seccionId))
            put("curso", JSONObject().put("id", cursoId))
            put("bimestre", JSONObject().put("id", bimestreId))
        }

        Log.d("ActividadService", "Enviando PUT para actualizar: $json")

        val body = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder().url("$BASE_URL/$id").put(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ActividadService", "Fallo al actualizar actividad: ${e.message}")
                callback()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("ActividadService", "Actividad actualizada, código: ${response.code}")
                callback()
            }
        })
    }

    fun eliminarActividad(id: Int, callback: () -> Unit) {
        val request = Request.Builder().url("$BASE_URL/$id").delete().build()
        Log.d("ActividadService", "Enviando DELETE para ID: $id")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ActividadService", "Fallo al eliminar actividad: ${e.message}")
                callback()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("ActividadService", "Actividad eliminada, código: ${response.code}")
                callback()
            }
        })
    }
}
