package com.example.edugtapp.network

import okhttp3.*
import org.json.JSONArray
import java.io.IOException

object CatalogoService {

    private val client = OkHttpClient()
    private const val BASE_URL = "https://eduapi-production.up.railway.app/api"

    fun obtenerGrados(onSuccess: (List<OpcionCatalogo>) -> Unit, onError: (String) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/grados")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onError("Error de red: ${e.message}")
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val jsonArray = JSONArray(body)
                    val resultado = mutableListOf<OpcionCatalogo>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        resultado.add(OpcionCatalogo(obj.getInt("id"), obj.getString("nombre")))
                    }
                    onSuccess(resultado)
                } else {
                    onError("Código: ${response.code}")
                }
            }
        })
    }

    fun obtenerSecciones(onSuccess: (List<OpcionCatalogo>) -> Unit, onError: (String) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/secciones")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onError("Error de red: ${e.message}")
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val jsonArray = JSONArray(body)
                    val resultado = mutableListOf<OpcionCatalogo>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        resultado.add(OpcionCatalogo(obj.getInt("id"), obj.getString("nombre")))
                    }
                    onSuccess(resultado)
                } else {
                    onError("Código: ${response.code}")
                }
            }
        })
    }

    data class OpcionCatalogo(val id: Int, val nombre: String) {
        override fun toString(): String = nombre
    }
}
