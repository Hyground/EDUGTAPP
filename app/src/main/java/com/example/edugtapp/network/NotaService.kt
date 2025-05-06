package com.example.edugtapp.network

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object NotaService {
    private const val BASE_URL = "https://eduapi-production.up.railway.app/api/notas"
    private val client = OkHttpClient()

    fun obtenerEvaluacionesConNota(
        gradoId: Int,
        seccionId: Int,
        cursoId: Int,
        bimestreId: Int,
        cui: String,
        callback: (JSONArray) -> Unit
    ) {
        val url =
            "$BASE_URL/evaluaciones-con-nota?gradoId=$gradoId&seccionId=$seccionId&cursoId=$cursoId&bimestreId=$bimestreId&cui=$cui"

        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(JSONArray())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                try {
                    callback(JSONArray(body ?: "[]"))
                } catch (e: Exception) {
                    callback(JSONArray())
                }
            }
        })
    }

    fun enviarNota(notaJson: JSONObject, callback: (Boolean) -> Unit) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = notaJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(BASE_URL)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.isSuccessful)
            }
        })
    }
}
