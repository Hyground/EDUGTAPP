package com.example.edugtapp.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.edugtapp.R
import com.example.edugtapp.network.ActividadService
import com.example.edugtapp.network.CursoService
import org.json.JSONArray

class RegistrarActividadActivity : AppCompatActivity() {

    private lateinit var spinnerBimestre: Spinner
    private lateinit var spinnerCurso: Spinner
    private lateinit var listActividades: ListView
    private lateinit var btnAdd: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var tvGradoSeccion: TextView

    private val bimestres = listOf(
        Pair(1, "I"),
        Pair(2, "II"),
        Pair(3, "III"),
        Pair(4, "IV")
    )

    private var gradoId = 0
    private var seccionId = 0
    private var cursoIds = listOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_actividad)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Recuperar datos del intent
        gradoId = intent.getIntExtra("GRADO_ID", 0)
        seccionId = intent.getIntExtra("SECCION_ID", 0)
        val gradoTexto = intent.getStringExtra("GRADO_DOCENTE") ?: "-"
        val seccionTexto = intent.getStringExtra("SECCION_DOCENTE") ?: "-"

        // Referencias
        spinnerBimestre = findViewById(R.id.spinnerBimestre)
        spinnerCurso = findViewById(R.id.spinnerCurso)
        listActividades = findViewById(R.id.listActividades)
        btnAdd = findViewById(R.id.btnAdd)
        tvGradoSeccion = findViewById(R.id.tvGradoSeccion)

        tvGradoSeccion.text = "Grado: $gradoTexto / Sección: $seccionTexto"

        // Spinner Bimestre con opción por defecto
        val bimestreOpciones = listOf("Selecionar") + bimestres.map { it.second }
        spinnerBimestre.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bimestreOpciones)

        // Cargar cursos dinámicamente
        cargarCursos()

        btnAdd.setOnClickListener {
            mostrarFormularioRegistro()
        }
    }

    private fun cargarCursos() {
        CursoService.obtenerCursosPorGrado(gradoId) { jsonArray ->
            runOnUiThread {
                val nombres = mutableListOf("Selecionar")
                val ids = mutableListOf<Int>()
                for (i in 0 until jsonArray.length()) {
                    val curso = jsonArray.getJSONObject(i)
                    nombres.add(curso.getString("nombreCurso"))
                    ids.add(curso.getInt("id"))
                }
                cursoIds = ids
                spinnerCurso.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)

                // Listeners
                spinnerCurso.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                        if (position == 0 || spinnerBimestre.selectedItemPosition == 0) return
                        val cursoId = cursoIds.getOrNull(position - 1) ?: return
                        val bimestreId = bimestres.getOrNull(spinnerBimestre.selectedItemPosition - 1)?.first ?: return
                        cargarActividades(gradoId, seccionId, cursoId, bimestreId)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                spinnerBimestre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                        if (position == 0 || spinnerCurso.selectedItemPosition == 0) return
                        val bimestreId = bimestres.getOrNull(position - 1)?.first ?: return
                        val cursoId = cursoIds.getOrNull(spinnerCurso.selectedItemPosition - 1) ?: return
                        cargarActividades(gradoId, seccionId, cursoId, bimestreId)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }
    }

    private fun cargarActividades(gradoId: Int, seccionId: Int, cursoId: Int, bimestreId: Int) {
        ActividadService.obtenerActividades(gradoId, seccionId, cursoId, bimestreId) { jsonArray ->
            runOnUiThread {
                val actividades = mutableListOf<Map<String, String>>()
                for (i in 0 until jsonArray.length()) {
                    val act = jsonArray.getJSONObject(i)
                    actividades.add(
                        mapOf(
                            "nombre" to act.getString("nombre"),
                            "tipo" to act.getString("tipo"),
                            "ponderacion" to act.getDouble("ponderacion").toString()
                        )
                    )
                }

                val adapter = object : SimpleAdapter(
                    this,
                    actividades,
                    R.layout.list_item_actividad,
                    arrayOf("nombre", "tipo", "ponderacion"),
                    intArrayOf(R.id.tvNombre, R.id.tvTipo, R.id.tvPonderacion)
                ) {
                    override fun setViewText(v: TextView?, text: String?) {
                        when (v?.id) {
                            R.id.tvTipo -> v.text = "Tipo: $text"
                            R.id.tvPonderacion -> v.text = "Ponderación: $text"
                            else -> super.setViewText(v, text)
                        }
                    }
                }

                listActividades.adapter = adapter
            }
        }
    }

    private fun mostrarFormularioRegistro() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nueva_actividad, null)
        val dialog = android.app.AlertDialog.Builder(this).setView(dialogView).create()

        val edtNombre = dialogView.findViewById<EditText>(R.id.edtNombre)
        val edtPonderacion = dialogView.findViewById<EditText>(R.id.edtPonderacion)
        val spinnerTipo = dialogView.findViewById<Spinner>(R.id.spinnerTipo)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardarActividad)

        spinnerTipo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Actividad", "Evaluacion"))

        btnGuardar.setOnClickListener {
            val nombre = edtNombre.text.toString().trim()
            val ponderacion = edtPonderacion.text.toString().toDoubleOrNull() ?: 0.0
            val tipo = spinnerTipo.selectedItem.toString()
            val bimestreId = bimestres.getOrNull(spinnerBimestre.selectedItemPosition - 1)?.first ?: return@setOnClickListener
            val cursoId = cursoIds.getOrNull(spinnerCurso.selectedItemPosition - 1) ?: return@setOnClickListener

            if (nombre.isEmpty() || ponderacion <= 0.0) {
                Toast.makeText(this, "Complete los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ActividadService.crearActividad(gradoId, seccionId, cursoId, bimestreId, nombre, tipo, ponderacion) {
                runOnUiThread {
                    Toast.makeText(this, "Actividad registrada", Toast.LENGTH_SHORT).show()
                    cargarActividades(gradoId, seccionId, cursoId, bimestreId)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }
}
