package com.example.edugtapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.edugtapp.R
import com.example.edugtapp.model.DocenteInfo
import com.example.edugtapp.network.ActividadService
import com.example.edugtapp.network.CursoService
import com.google.android.material.bottomsheet.BottomSheetDialog

class RegistrarActividadActivity : AppCompatActivity() {

    private lateinit var tvSeleccionBimestre: TextView
    private lateinit var tvSeleccionCurso: TextView
    private lateinit var listActividades: ListView
    private lateinit var btnAdd: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var tvGradoSeccion: TextView

    private lateinit var formulario: LinearLayout
    private lateinit var edtNombre: EditText
    private lateinit var edtPonderacion: EditText
    private lateinit var rgTipo: RadioGroup
    private lateinit var rbActividad: RadioButton
    private lateinit var rbEvaluacion: RadioButton
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    private val bimestres = listOf(Pair(1, "I BIMESTRE"), Pair(2, "II BIMESTRE"), Pair(3, "III BIMESTRE"), Pair(4, "IV BIMESTRE"))
    private lateinit var docenteInfo: DocenteInfo
    private var cursoIds = listOf<Int>()
    private var selectedCursoId: Int? = null
    private var selectedBimestreId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_actividad)

        docenteInfo = DocenteInfo.fromIntent(intent)

        inicializarVista()
        tvGradoSeccion.text = "Grado: ${docenteInfo.grado}                     Sección: ${docenteInfo.seccion}"

        cargarCursos()
        btnAdd.setOnClickListener { mostrarFormulario() }
        btnCancelar.setOnClickListener { limpiarFormulario() }
        btnGuardar.setOnClickListener { guardarActividad() }

        tvSeleccionBimestre.setOnClickListener { mostrarBimestres() }
        tvSeleccionCurso.setOnClickListener { mostrarCursos() }
    }

    private fun inicializarVista() {
        tvSeleccionBimestre = findViewById(R.id.tvSeleccionBimestre)
        tvSeleccionCurso = findViewById(R.id.tvSeleccionCurso)
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
        CursoService.obtenerCursosPorGrado(docenteInfo.gradoId) { jsonArray ->
            runOnUiThread {
                val nombres = mutableListOf<String>()
                val ids = mutableListOf<Int>()
                for (i in 0 until jsonArray.length()) {
                    val curso = jsonArray.getJSONObject(i)
                    nombres.add(curso.getString("nombreCurso"))
                    ids.add(curso.getInt("id"))
                }
                cursoIds = ids
                tvSeleccionCurso.tag = nombres
            }
        }
    }

    private fun mostrarBimestres() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_lista, null)
        val list = view.findViewById<ListView>(R.id.listaOpciones)

        val opciones = bimestres.map { it.second }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, opciones)
        list.adapter = adapter
        list.choiceMode = ListView.CHOICE_MODE_SINGLE

        selectedBimestreId?.let { id ->
            val index = bimestres.indexOfFirst { it.first == id }
            if (index != -1) list.setItemChecked(index, true)
        }

        list.setOnItemClickListener { _, _, position, _ ->
            selectedBimestreId = bimestres[position].first
            tvSeleccionBimestre.text = bimestres[position].second
            dialog.dismiss()
            refrescarActividades()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun mostrarCursos() {
        val nombres = tvSeleccionCurso.tag as? List<String> ?: return

        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_lista, null)
        val list = view.findViewById<ListView>(R.id.listaOpciones)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, nombres)
        list.adapter = adapter
        list.choiceMode = ListView.CHOICE_MODE_SINGLE

        selectedCursoId?.let { id ->
            val index = cursoIds.indexOf(id)
            if (index != -1) list.setItemChecked(index, true)
        }

        list.setOnItemClickListener { _, _, position, _ ->
            selectedCursoId = cursoIds.getOrNull(position)
            tvSeleccionCurso.text = nombres[position]
            dialog.dismiss()
            refrescarActividades()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun refrescarActividades() {
        val cursoId = selectedCursoId ?: return
        val bimestreId = selectedBimestreId ?: return
        cargarActividades(docenteInfo.gradoId, docenteInfo.seccionId, cursoId, bimestreId)
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

        val bimestreId = selectedBimestreId ?: return
        val cursoId = selectedCursoId ?: return

        if (nombre.isEmpty() || tipo.isEmpty() || ponderacion <= 0.0) {
            Toast.makeText(this, "Complete todos los campos correctamente", Toast.LENGTH_SHORT).show()
            return
        }

        ActividadService.crearActividad(docenteInfo.gradoId, docenteInfo.seccionId, cursoId, bimestreId, nombre, tipo, ponderacion) {
            runOnUiThread {
                Toast.makeText(this, "Actividad registrada", Toast.LENGTH_SHORT).show()
                limpiarFormulario()
                refrescarActividades()
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
