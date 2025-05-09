package com.example.edugtapp.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.R
import com.example.edugtapp.model.DocenteInfo
import com.example.edugtapp.network.Estudiante
import com.example.edugtapp.network.EstudianteService
import com.example.edugtapp.network.NotaService
import org.json.JSONObject
import android.util.Log

class RegistrarNotaDesdeActividadActivity : AppCompatActivity() {

    private lateinit var tvNombreActividad: TextView
    private lateinit var listEstudiantes: ListView
    private lateinit var btnGuardarNotas: Button

    private var idActividad: Int = -1
    private var gradoId: Int = -1
    private var seccionId: Int = -1
    private var nombreActividad: String = ""
    private lateinit var docenteInfo: DocenteInfo

    private val estudiantes = mutableListOf<Estudiante>()
    private val calificaciones = mutableMapOf<String, Double>() // CUI -> Nota

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_nota_actividad)

        tvNombreActividad = findViewById(R.id.tvNombreActividad)
        listEstudiantes = findViewById(R.id.listEstudiantesActividad)
        btnGuardarNotas = findViewById(R.id.btnGuardarNotasActividad)

        idActividad = intent.getIntExtra("idActividad", -1)
        gradoId = intent.getIntExtra("gradoId", -1)
        seccionId = intent.getIntExtra("seccionId", -1)
        nombreActividad = intent.getStringExtra("nombreActividad") ?: ""
        docenteInfo = DocenteInfo.fromIntent(intent)

        if (idActividad == -1 || gradoId == -1 || seccionId == -1) {
            Toast.makeText(this, "Datos incompletos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvNombreActividad.text = "Actividad: $nombreActividad"
        cargarEstudiantesYNotas()

        btnGuardarNotas.setOnClickListener { guardarNotas() }
    }

    private fun cargarEstudiantesYNotas() {
        EstudianteService.obtenerEstudiantesPorDocente(docenteInfo.docenteId) { lista ->
            NotaService.obtenerNotasDeActividad(idActividad) { notasArray ->
                runOnUiThread {
                    Log.d("NotasDebug", "Notas recibidas: ${notasArray.length()}")
                    val notasExistentes = mutableMapOf<String, Double>()
                    for (i in 0 until notasArray.length()) {
                        val obj = notasArray.getJSONObject(i)
                        val estudianteObj = obj.optJSONObject("estudiante")
                        val cui = estudianteObj?.optString("cui") ?: ""
                        val nota = obj.optDouble("nota", -1.0)
                        Log.d("NotasDebug", "Nota para $cui: $nota")
                        if (cui.isNotEmpty() && nota >= 0) {
                            notasExistentes[cui] = nota
                        }
                    }

                    val filtrados = lista.filter {
                        it.gradoId == gradoId && it.seccionId == seccionId && it.activo
                    }

                    estudiantes.clear()
                    estudiantes.addAll(filtrados)

                    listEstudiantes.adapter = object : BaseAdapter() {
                        override fun getCount() = estudiantes.size
                        override fun getItem(position: Int) = estudiantes[position]
                        override fun getItemId(position: Int) = position.toLong()

                        override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup?): android.view.View {
                            val view = layoutInflater.inflate(R.layout.item_estudiante_calificacion, parent, false)
                            val estudiante = estudiantes[position]

                            view.findViewById<TextView>(R.id.tvNombreEstudiante).text =
                                "${estudiante.nombre} ${estudiante.apellido}"

                            val edtNota = view.findViewById<EditText>(R.id.edtNotaEstudiante)

                            val notaActual = notasExistentes[estudiante.cui]
                            if (notaActual != null) {
                                edtNota.setText(notaActual.toString())
                                calificaciones[estudiante.cui] = notaActual
                            }

                            edtNota.setOnFocusChangeListener { _, hasFocus ->
                                if (!hasFocus) {
                                    val nota = edtNota.text.toString().toDoubleOrNull()
                                    if (nota != null) {
                                        calificaciones[estudiante.cui] = nota
                                    }
                                }
                            }

                            return view
                        }
                    }
                }
            }
        }
    }


    private fun guardarNotas() {
        if (calificaciones.isEmpty()) {
            Toast.makeText(this, "No se ingresaron notas", Toast.LENGTH_SHORT).show()
            return
        }

        var total = calificaciones.size
        var exitosas = 0
        var fallidas = 0

        for ((cui, nota) in calificaciones) {
            val json = JSONObject().apply {
                put("cui", cui)
                put("actividadId", idActividad)
                put("nota", nota)
            }

            NotaService.enviarNota(json) { exito ->
                runOnUiThread {
                    if (exito) exitosas++ else fallidas++

                    if (exitosas + fallidas == total) {
                        if (fallidas == 0) {
                            Toast.makeText(this, "Todas las notas guardadas correctamente", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "$exitosas notas guardadas, $fallidas fallidas", Toast.LENGTH_LONG).show()
                        }
                        finish()
                    }
                }
            }
        }
    }
}
