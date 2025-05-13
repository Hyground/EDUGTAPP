package com.example.edugtapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.edugtapp.R
import com.example.edugtapp.model.DocenteInfo
import com.example.edugtapp.network.Estudiante
import com.example.edugtapp.network.EstudianteService

class RegistrarNotasActivity : AppCompatActivity() {

    private lateinit var docenteInfo: DocenteInfo
    private lateinit var tvGrado: TextView
    private lateinit var tvSeccion: TextView
    private lateinit var listaEstudiantes: ListView

    private val estudiantes = mutableListOf<Estudiante>()
    private lateinit var adapter: BaseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_notas)

        docenteInfo = DocenteInfo.fromIntent(intent)

        tvGrado = findViewById(R.id.tvGrado)
        tvSeccion = findViewById(R.id.tvSeccion)
        listaEstudiantes = findViewById(R.id.listaEstudiantes)

        tvGrado.text = "Grado: ${docenteInfo.grado}"
        tvSeccion.text = "Sección: ${docenteInfo.seccion}"

        inicializarAdapter()
        listaEstudiantes.adapter = adapter

        cargarEstudiantes(docenteInfo.docenteId)
    }

    private fun inicializarAdapter() {
        adapter = object : BaseAdapter() {
            override fun getCount() = estudiantes.size
            override fun getItem(position: Int) = estudiantes[position]
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view = convertView ?: layoutInflater.inflate(R.layout.list_item_estudiante_calificacion, parent, false)
                val estudiante = estudiantes[position]

                view.findViewById<TextView>(R.id.tvNombreEstudiante).text =
                    "${estudiante.nombre} ${estudiante.apellido}"

                view.findViewById<Button>(R.id.btnCalificar).setOnClickListener {
                    val intent = Intent(this@RegistrarNotasActivity, RegistrarNotaDesdeEstudianteActivity::class.java)
                    intent.putExtra("CUI_ESTUDIANTE", estudiante.cui)
                    startActivity(intent)
                }

                return view
            }
        }
    }

    private fun cargarEstudiantes(docenteId: Int) {
        EstudianteService.obtenerEstudiantesPorDocente(docenteId) { lista ->
            runOnUiThread {
                estudiantes.clear()
                estudiantes.addAll(lista)
                adapter.notifyDataSetChanged()
            }
        }
    }
}
