package com.example.proyectopersonal

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class HistorialActivity : AppCompatActivity() {
    private lateinit var textoResumen: TextView
    private lateinit var recycler: RecyclerView
    private val dao by lazy { AppDatabase.obtener(this).componenteDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        textoResumen = findViewById(R.id.textoResumenHistorial)
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

    private fun cargarDatosYActualizar(categoriaFiltro: String? = null) {
        lifecycleScope.launch {
            val listaComponentes = if (categoriaFiltro != null) {
                dao.obtenerPorCategoria(categoriaFiltro).toMutableList()
            } else {
                dao.obtenerTodos().toMutableList()
            }

            val totalComponentesBD = dao.contarComponentes()

            val sumaPrecios = listaComponentes.sumOf { it.precio }
            val totalFormateado = String.format("%.2f", sumaPrecios)

            textoResumen.text = "Total en BD: $totalComponentesBD | Mostrados: ${listaComponentes.size} | Precio: $totalFormateado €"

            recycler.adapter = HistorialAdapter(listaComponentes) { componenteParaBorrar ->
                lifecycleScope.launch {
                    dao.borrar(componenteParaBorrar)
                    cargarDatosYActualizar(categoriaFiltro)
                }
            }
        }
    }
}