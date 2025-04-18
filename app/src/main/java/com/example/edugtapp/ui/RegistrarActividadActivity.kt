package com.example.edugtapp.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.edugtapp.R
import com.example.edugtapp.network.ActividadService
import com.example.edugtapp.network.CursoService

class RegistrarActividadActivity : AppCompatActivity() {

    // UI Elementos principales
    private lateinit var spinnerBimestre: Spinner
    private lateinit var spinnerCurso: Spinner
    private lateinit var listActividades: ListView
    private lateinit var btnAdd: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var tvGradoSeccion: TextView

    // Formulario
    private lateinit var formulario: LinearLayout
    private lateinit var edtNombre: EditText
    private lateinit var edtPonderacion: EditText
    private lateinit var rgTipo: RadioGroup
    private lateinit var rbActividad: RadioButton
    private lateinit var rbEvaluacion: RadioButton
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    // Datos
    private val bimestres = listOf(Pair(1, "I"), Pair(2, "II"), Pair(3, "III"), Pair(4, "IV"))
    private var gradoId = 0
    private var seccionId = 0
    private var cursoIds = listOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_actividad)

        // Inicializar datos del docente
        gradoId = intent.getIntExtra("GRADO_ID", 0)
        seccionId = intent.getIntExtra("SECCION_ID", 0)
        val gradoTexto = intent.getStringExtra("GRADO_DOCENTE") ?: "-"
        val seccionTexto = intent.getStringExtra("SECCION_DOCENTE") ?: "-"

        // Inicializar UI
        inicializarVista()
        tvGradoSeccion.text = "Grado: $gradoTexto                     Sección: $seccionTexto"

        spinnerBimestre.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Seleccionar") + bimestres.map { it.second })
        cargarCursos()

        btnAdd.setOnClickListener { mostrarFormulario() }
        btnCancelar.setOnClickListener { limpiarFormulario() }
        btnGuardar.setOnClickListener { guardarActividad() }
    }

    private fun inicializarVista() {
        spinnerBimestre = findViewById(R.id.spinnerBimestre)
        spinnerCurso = findViewById(R.id.spinnerCurso)
        listActividades = findViewById(R.id.listActividades)
        btnAdd = findViewById(R.id.btnAdd)
        tvGradoSeccion = findViewById(R.id.tvGradoSeccion)

        formulario = findViewById(R.id.formularioActividad)
        edtNombre = findViewById(R.id.edtNombre)
        edtPonderacion = findViewById(R.id.edtPonderacion)
        rgTipo = findViewById(R.id.rgTipo)
        rbActividad = findViewById(R.id.rbActividad)
        rbEvaluacion = findViewById(R.id.rbEvaluacion)
        btnGuardar = findViewById(R.id.btnGuardarActividad)
        btnCancelar = findViewById(R.id.btnCancelarActividad)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun cargarCursos() {
        CursoService.obtenerCursosPorGrado(gradoId) { jsonArray ->
            runOnUiThread {
                val nombres = mutableListOf("Seleccionar")
                val ids = mutableListOf<Int>()

                for (i in 0 until jsonArray.length()) {
                    val curso = jsonArray.getJSONObject(i)
                    nombres.add(curso.getString("nombreCurso"))
                    ids.add(curso.getInt("id"))
                }

                cursoIds = ids
                spinnerCurso.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)

                val listener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val cursoId = cursoIds.getOrNull(spinnerCurso.selectedItemPosition - 1)
                        val bimestreId = bimestres.getOrNull(spinnerBimestre.selectedItemPosition - 1)?.first
                        if (cursoId != null && bimestreId != null) {
                            cargarActividades(gradoId, seccionId, cursoId, bimestreId)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                spinnerCurso.onItemSelectedListener = listener
                spinnerBimestre.onItemSelectedListener = listener
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

                listActividades.adapter = object : SimpleAdapter(
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
            }
        }
    }

    private fun guardarActividad() {
        val nombre = edtNombre.text.toString().trim()
        val ponderacion = edtPonderacion.text.toString().toDoubleOrNull() ?: 0.0
        val tipo = when {
            rbActividad.isChecked -> "Actividad"
            rbEvaluacion.isChecked -> "Evaluacion"
            else -> ""
        }

        val bimestreId = bimestres.getOrNull(spinnerBimestre.selectedItemPosition - 1)?.first ?: return
        val cursoId = cursoIds.getOrNull(spinnerCurso.selectedItemPosition - 1) ?: return

        if (nombre.isEmpty() || tipo.isEmpty() || ponderacion <= 0.0) {
            Toast.makeText(this, "Complete todos los campos correctamente", Toast.LENGTH_SHORT).show()
            return
        }

        ActividadService.crearActividad(gradoId, seccionId, cursoId, bimestreId, nombre, tipo, ponderacion) {
            runOnUiThread {
                Toast.makeText(this, "Actividad registrada", Toast.LENGTH_SHORT).show()
                limpiarFormulario()
                cargarActividades(gradoId, seccionId, cursoId, bimestreId)
            }
        }
    }

    private fun mostrarFormulario() {
        formulario.visibility = View.VISIBLE
        btnAdd.hide()
    }

    private fun limpiarFormulario() {
        formulario.visibility = View.GONE
        btnAdd.show()
        edtNombre.text.clear()
        edtPonderacion.text.clear()
        rgTipo.clearCheck()
    }
}
