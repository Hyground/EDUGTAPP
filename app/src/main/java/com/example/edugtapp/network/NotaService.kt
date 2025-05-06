package com.example.edugtapp.network

import android.util.Log
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
        Log.d("NotaService", "URL: $url")

        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NotaService", "Fallo de red: ${e.message}")
                callback(JSONArray())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("NotaService", "Respuesta: $body")

                try {
                    callback(JSONArray(body ?: "[]"))
                } catch (e: Exception) {
                    Log.e("NotaService", "Error al convertir JSON: ${e.message}")
                    callback(JSONArray())
                }
            }
        })
    }

    // ✅ ESTA ES LA FUNCIÓN QUE ACEPTA UN JSONObject DIRECTAMENTE
    fun enviarNota(notaJson: JSONObject, callback: (Boolean) -> Unit) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = notaJson.toString().toRequestBody(mediaType)

        Log.d("NotaService", "JSON enviado: ${notaJson.toString()}")

        val request = Request.Builder()
            .url(BASE_URL)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NotaService", "Error al enviar nota: ${e.message}")
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("NotaService", "Respuesta guardar: ${response.code}")
                callback(response.isSuccessful)
            }
        })
    }
}
