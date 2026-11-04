package com.example.proyectopersonal

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class HistorialActivity : ComponentActivity() {
    private lateinit var textoResumen: TextView
    private lateinit var recycler: RecyclerView
    private val dao by lazy { AppDatabase.obtener(this).componenteDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        textoResumen = findViewById<TextView>(R.id.textoResumenHistorial)
        recycler = findViewById<RecyclerView>(R.id.recyclerHistorial)
        val botonVolver = findViewById<Button>(R.id.botonVolverHistorial)

        recycler.layoutManager = LinearLayoutManager(this)

        botonVolver.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatosYActualizar()
    }

    private fun cargarDatosYActualizar() {
        lifecycleScope.launch {
            // Obtenemos los datos directamente de la base de datos[cite: 3, 4]
            val listaComponentes = dao.obtenerTodos().toMutableList()

            recycler.adapter = HistorialAdapter(listaComponentes) { componenteParaBorrar ->
                // Al pulsar eliminar, borramos de la BD y recargamos la lista[cite: 3, 5]
                lifecycleScope.launch {
                    dao.borrar(componenteParaBorrar)
                    cargarDatosYActualizar()
                }
            }

            actualizarResumen(listaComponentes)
        }
    }

    private fun actualizarResumen(lista: List<Componente>) {
        val cantidad = lista.size
        val total = lista.sumOf { it.precio }
        val totalFormateado = String.format("%.2f", total)
        textoResumen.text = "Total: $cantidad componentes | $totalFormateado €"
    }
}