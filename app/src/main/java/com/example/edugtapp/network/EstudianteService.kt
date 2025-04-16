package com.example.edugtapp.network

import okhttp3.*
import org.json.JSONArray
import java.io.IOException

object EstudianteService {

    fun obtenerEstudiantesPorDocente(docenteId: Int, callback: (List<String>) -> Unit) {
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
                            val lista = mutableListOf<String>()
                            val jsonArray = JSONArray(it)
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val nombre = obj.optString("nombre") + " " + obj.optString("apellido")
                                val codigo = obj.optString("codigoPersonal")
                                val cui = obj.optString("cui")
                                val texto = "$nombre\nCódigo: $codigo\nCUI No.: $cui"
                                lista.add(texto)
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
