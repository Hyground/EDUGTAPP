package com.example.edugtapp.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.R
import com.example.edugtapp.network.ActividadService
import com.example.edugtapp.network.CursoService
import org.json.JSONArray

class RegistrarActividadActivity : AppCompatActivity() {

    private lateinit var tvGradoSeccion: TextView
    private lateinit var spinnerBimestre: Spinner
    private lateinit var spinnerCurso: Spinner
    private lateinit var btnCargar: Button
    private lateinit var listActividades: ListView
    private lateinit var edtNombre: EditText
    private lateinit var edtPonderacion: EditText
    private lateinit var spinnerTipo: Spinner
    private lateinit var btnGuardar: Button

    private val bimestres = listOf(
        Pair(1, "I UNIDAD"),
        Pair(2, "II UNIDAD"),
        Pair(3, "III UNIDAD"),
        Pair(4, "IV UNIDAD")
    )

    private var gradoId = 0
    private var seccionId = 0
    private var cursoIds = listOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_actividad)

        gradoId = intent.getIntExtra("GRADO_ID", 0)
        seccionId = intent.getIntExtra("SECCION_ID", 0)
        val gradoTexto = intent.getStringExtra("GRADO_DOCENTE") ?: "-"
        val seccionTexto = intent.getStringExtra("SECCION_DOCENTE") ?: "-"

        tvGradoSeccion = findViewById(R.id.tvGradoSeccion)
        spinnerBimestre = findViewById(R.id.spinnerBimestre)
        spinnerCurso = findViewById(R.id.spinnerCurso)
        btnCargar = findViewById(R.id.btnCargarActividades)
        listActividades = findViewById(R.id.listActividades)
        edtNombre = findViewById(R.id.edtNombre)
        edtPonderacion = findViewById(R.id.edtPonderacion)
        spinnerTipo = findViewById(R.id.spinnerTipo)
        btnGuardar = findViewById(R.id.btnGuardarActividad)

        tvGradoSeccion.text = "Grado: $gradoTexto / Sección: $seccionTexto"

        spinnerBimestre.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bimestres.map { it.second })
        spinnerTipo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Actividad", "Evaluacion"))

        cargarCursos()

        btnCargar.setOnClickListener {
            val bimestreId = bimestres[spinnerBimestre.selectedItemPosition].first
            val cursoId = cursoIds.getOrNull(spinnerCurso.selectedItemPosition) ?: return@setOnClickListener
            cargarActividades(gradoId, seccionId, cursoId, bimestreId)
        }

        btnGuardar.setOnClickListener {
            val nombre = edtNombre.text.toString().trim()
            val ponderacion = edtPonderacion.text.toString().toDoubleOrNull() ?: 0.0
            val tipo = spinnerTipo.selectedItem.toString()
            val bimestreId = bimestres[spinnerBimestre.selectedItemPosition].first
            val cursoId = cursoIds.getOrNull(spinnerCurso.selectedItemPosition) ?: return@setOnClickListener

            if (nombre.isEmpty() || ponderacion <= 0.0) {
                Toast.makeText(this, "Complete los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ActividadService.crearActividad(gradoId, seccionId, cursoId, bimestreId, nombre, tipo, ponderacion) {
                runOnUiThread {
                    Toast.makeText(this, "Actividad registrada", Toast.LENGTH_SHORT).show()
                    edtNombre.text.clear()
                    edtPonderacion.text.clear()
                    cargarActividades(gradoId, seccionId, cursoId, bimestreId)
                }
            }
        }
    }

    private fun cargarCursos() {
        CursoService.obtenerCursosPorGrado(gradoId) { jsonArray ->
            runOnUiThread {
                val nombres = mutableListOf<String>()
                val ids = mutableListOf<Int>()
                for (i in 0 until jsonArray.length()) {
                    val curso = jsonArray.getJSONObject(i)
                    nombres.add(curso.getString("nombreCurso"))
                    ids.add(curso.getInt("id"))
                }
                cursoIds = ids
                spinnerCurso.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
            }
        }
    }

    private fun cargarActividades(gradoId: Int, seccionId: Int, cursoId: Int, bimestreId: Int) {
        ActividadService.obtenerActividades(gradoId, seccionId, cursoId, bimestreId) { jsonArray ->
            runOnUiThread {
                val actividades = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val act = jsonArray.getJSONObject(i)
                    val nombre = act.getString("nombre")
                    val tipo = act.getString("tipo")
                    val pond = act.getDouble("ponderacion")
                    actividades.add("$nombre\nTipo: $tipo - Ponderación: $pond")
                }
                listActividades.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, actividades)
            }
        }
    }
}
