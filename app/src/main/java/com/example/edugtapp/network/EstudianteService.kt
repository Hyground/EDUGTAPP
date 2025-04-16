package com.example.edugtapp.network

import okhttp3.*
import org.json.JSONArray
import java.io.IOException

data class Estudiante(
    val nombre: String,
    val apellido: String,
    val codigo: String,
    val cui: String
)

object EstudianteService {

    fun obtenerEstudiantesPorDocente(docenteId: Int, callback: (List<Estudiante>) -> Unit) {
        val url = "https://eduapi-production.up.railway.app/api/estudiantes/por-docente/$docenteId"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(emptyList())
            }

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
                                    cui = obj.optString("cui")
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
}
