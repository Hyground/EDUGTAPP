package com.example.edugtapp.network

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

object DocenteService {

    private const val BASE_URL = "https://eduapi-production.up.railway.app/api/fotosdocentes"
    private val client = OkHttpClient()

    fun obtenerFotoDocente(docenteId: Int, callback: (String?) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/$docenteId")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(null)
                        return
                    }

                    val fotoUrl = try {
                        val json = JSONObject(it.body?.string() ?: "")
                        json.getString("fotoUrl")
                    } catch (e: Exception) {
                        null
                    }

                    callback(fotoUrl)
                }
            }
        })
    }

    fun subirFotoDocente(docenteId: Int, imagen: File, callback: (Boolean) -> Unit) {
        val mediaType = "image/*".toMediaTypeOrNull()
        val requestBody = imagen.asRequestBody(mediaType)

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("archivo", imagen.name, requestBody)
            .build()

        val request = Request.Builder()
            .url("$BASE_URL/subir/$docenteId")
            .post(multipartBody)
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
