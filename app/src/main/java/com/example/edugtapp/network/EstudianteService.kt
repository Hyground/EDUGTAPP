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

    fun obtenerEstudiantesPorDocente(docenteId: Int, callback: (List<Estudiante>) -> Unit) {
        val url = "https://eduapi-production.up.railway.app/api/estudiantes/por-docente/$docenteId"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback(emptyList())

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let {
                        try {
                            val lista = mutableListOf<Estudiante>()
                            val jsonArray = JSONArray(it)
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val estudiante = Estudiante(
                                    nombre = obj.optString("nombre"),
                                    apellido = obj.optString("apellido"),
                                    codigo = obj.optString("codigoPersonal"),
                                    cui = obj.optString("cui"),
                                    gradoId = obj.optJSONObject("grado")?.optInt("id") ?: 0,
                                    seccionId = obj.optJSONObject("seccion")?.optInt("id") ?: 0,
                                    activo = obj.optBoolean("borradoLogico", true)
                                )
                                lista.add(estudiante)
                            }
                            callback(lista)
                        } catch (_: Exception) {
                            callback(emptyList())
                        }
                    } ?: callback(emptyList())
                } else {
                    callback(emptyList())
                }
            }
        })
    }

    fun actualizarEstudiante(estudiante: Estudiante, callback: (Boolean, String) -> Unit) {
        val url = "https://eduapi-production.up.railway.app/api/estudiantes/${estudiante.cui}"
        val json = JSONObject().apply {
            put("cui", estudiante.cui)
            put("codigoPersonal", estudiante.codigo)
            put("nombre", estudiante.nombre)
            put("apellido", estudiante.apellido)
            put("borradoLogico", estudiante.activo)
            put("grado", JSONObject().put("id", estudiante.gradoId))
            put("seccion", JSONObject().put("id", estudiante.seccionId))
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Error de red: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: "Respuesta vacía"
                if (response.isSuccessful) {
                    callback(true, "Actualización exitosa")
                } else {
                    callback(false, "Error ${response.code}: $body")
                }
            }
        })
    }

    fun desactivarEstudiante(cui: String, callback: (Boolean, String) -> Unit) {
        val url = "https://eduapi-production.up.railway.app/api/estudiantes/$cui/desactivar"
        val request = Request.Builder()
            .url(url)
            .patch("".toRequestBody())
            .addHeader("Content-Type", "application/json")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Error de red: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: "Respuesta vacía"
                if (response.isSuccessful) {
                    callback(true, "Estudiante desactivado")
                } else {
                    callback(false, "Error ${response.code}: $body")
                }
            }
        })
    }

    fun crearEstudiante(estudiante: Estudiante, callback: (Boolean, String) -> Unit) {
        val url = "https://eduapi-production.up.railway.app/api/estudiantes"
        val json = JSONObject().apply {
            put("cui", estudiante.cui)
            put("codigoPersonal", estudiante.codigo)
            put("nombre", estudiante.nombre)
            put("apellido", estudiante.apellido)
            put("borradoLogico", true)
            put("grado", JSONObject().put("id", estudiante.gradoId))
            put("seccion", JSONObject().put("id", estudiante.seccionId))
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Error de red: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: "Respuesta vacía"
                if (response.isSuccessful) {
                    callback(true, "Estudiante creado")
                } else {
                    callback(false, "Error ${response.code}: $body")
                }
            }
        })
    }
}
