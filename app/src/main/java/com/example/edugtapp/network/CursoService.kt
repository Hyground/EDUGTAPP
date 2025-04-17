package com.example.edugtapp.network

import okhttp3.*
import org.json.JSONArray
import java.io.IOException

object CursoService {
    private const val BASE_URL = "https://eduapi-production.up.railway.app/api/cursos"
    private val client = OkHttpClient()

    fun obtenerCursosPorGrado(gradoId: Int, callback: (JSONArray) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/por-grado/$gradoId")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(JSONArray())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val jsonArray = JSONArray(body ?: "[]")
                callback(jsonArray)
            }
        })
    }
}
