package com.example.edugtapp.network

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

data class Estudiante(
    val nombre: String,
    val apellido: String,
    val codigo: String,
    val cui: String,
    val gradoId: Int = 0,
    val seccionId: Int = 0,
    val activo: Boolean = true
)

object EstudianteService {

    private const val BASE_URL = "https://eduapi-production.up.railway.app/api/estudiantes"
    private val client = OkHttpClient()

    fun obtenerEstudiantesPorDocente(docenteId: Int, callback: (List<Estudiante>) -> Unit) {
        val url = "$BASE_URL/por-docente/$docenteId"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback(emptyList())
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) return callback(emptyList())

                try {
                    val jsonArray = JSONArray(body)
                    val lista = (0 until jsonArray.length()).map { i ->
                        val obj = jsonArray.getJSONObject(i)
                        Estudiante(
                            nombre = obj.optString("nombre"),
                            apellido = obj.optString("apellido"),
                            codigo = obj.optString("codigoPersonal"),
                            cui = obj.optString("cui"),
                            gradoId = obj.optJSONObject("grado")?.optInt("id") ?: 0,
                            seccionId = obj.optJSONObject("seccion")?.optInt("id") ?: 0,
                            activo = obj.optBoolean("borradoLogico", true)
                        )
                    }
                    callback(lista)
                } catch (_: Exception) {
                    callback(emptyList())
                }
            }
        })
    }

    fun obtenerEstudiantePorCui(cui: String, callback: (Estudiante?) -> Unit) {
        val request = Request.Builder().url("$BASE_URL/$cui").get().build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback(null)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) return callback(null)
                try {
                    val obj = JSONObject(body)
                    val estudiante = Estudiante(
                        nombre = obj.optString("nombre"),
                        apellido = obj.optString("apellido"),
                        codigo = obj.optString("codigoPersonal"),
                        cui = obj.optString("cui"),
                        gradoId = obj.optJSONObject("grado")?.optInt("id") ?: 0,
                        seccionId = obj.optJSONObject("seccion")?.optInt("id") ?: 0,
                        activo = obj.optBoolean("borradoLogico", true)
                    )
                    callback(estudiante)
                } catch (_: Exception) {
                    callback(null)
                }
            }
        })
    }

    fun actualizarEstudiante(estudiante: Estudiante, callback: (Boolean, String) -> Unit) {
        val url = "$BASE_URL/${estudiante.cui}"
        val json = crearJson(estudiante)
        val request = Request.Builder()
            .url(url)
            .put(json.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .build()
        client.newCall(request).enqueue(crearCallback(callback))
    }

    fun crearEstudiante(estudiante: Estudiante, callback: (Boolean, String) -> Unit) {
        val json = crearJson(estudiante).apply { put("borradoLogico", true) }
        val request = Request.Builder()
            .url(BASE_URL)
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .build()
        client.newCall(request).enqueue(crearCallback(callback))
    }

    fun desactivarEstudiante(cui: String, callback: (Boolean, String) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/$cui/desactivar")
            .patch("".toRequestBody())
            .addHeader("Content-Type", "application/json")
            .build()
        client.newCall(request).enqueue(crearCallback(callback))
    }

    private fun crearJson(estudiante: Estudiante): JSONObject = JSONObject().apply {
        put("cui", estudiante.cui)
        put("codigoPersonal", estudiante.codigo)
        put("nombre", estudiante.nombre)
        put("apellido", estudiante.apellido)
        put("borradoLogico", estudiante.activo)
        put("grado", JSONObject().put("id", estudiante.gradoId))
        put("seccion", JSONObject().put("id", estudiante.seccionId))
    }

    private fun crearCallback(callback: (Boolean, String) -> Unit) = object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback(false, "Error de red: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            val body = response.body?.string() ?: "Respuesta vacía"
            if (response.isSuccessful) {
                callback(true, "Operación exitosa")
            } else {
                callback(false, "Error ${response.code}: $body")
            }
        }
    }
}
