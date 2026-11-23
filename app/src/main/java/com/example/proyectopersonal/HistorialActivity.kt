package com.example.proyectopersonal

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

// Actividad que muestra el historial de componentes guardados, su resumen y permite borrarlos
class HistorialActivity : AppCompatActivity() {
    // Referencias al texto de resumen y al listado visual de componentes
    private lateinit var textoResumen: TextView
    private lateinit var recycler: RecyclerView

    // Obtiene el DAO de componentes de forma perezosa desde la base de datos de Room
    private val dao by lazy { AppDatabase.obtener(this).componenteDao() }

    // Configura la pantalla, el diseño de la lista y el botón para volver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        textoResumen = findViewById(R.id.textoResumenHistorial)
        recycler = findViewById<RecyclerView>(R.id.recyclerHistorial)
        val botonVolver = findViewById<Button>(R.id.botonVolverHistorial)

        // Organiza los elementos del RecyclerView en una lista vertical
        recycler.layoutManager = LinearLayoutManager(this)

        // Cierra la actividad y regresa a la pantalla anterior
        botonVolver.setOnClickListener {
            finish()
        }
    }

    // Recarga los datos cada vez que la actividad vuelve a estar visible
    override fun onResume() {
        super.onResume()
        cargarDatosYActualizar()
    }

    // Consulta la base de datos, calcula el resumen de precios y actualiza el adaptador
    private fun cargarDatosYActualizar(categoriaFiltro: String? = null) {
        lifecycleScope.launch {
            // Obtiene la lista filtrada por categoría o todos los componentes si no hay filtro
            val listaComponentes = if (categoriaFiltro != null) {
                dao.obtenerPorCategoria(categoriaFiltro).toMutableList()
            } else {
                dao.obtenerTodos().toMutableList()
            }

            // Consulta el total de registros almacenados en la tabla
            val totalComponentesBD = dao.contarComponentes()

            // Suma los precios de la lista actual y formatea el total con dos decimales
            val sumaPrecios = listaComponentes.sumOf { it.precio }
            val totalFormateado = String.format("%.2f", sumaPrecios)

            textoResumen.text = "Total en BD: $totalComponentesBD | Mostrados: ${listaComponentes.size} | Precio: $totalFormateado €"

            // Asigna el adaptador y define qué ocurre al borrar un componente
            recycler.adapter = HistorialAdapter(listaComponentes) { componenteParaBorrar ->
                lifecycleScope.launch {
                    dao.borrar(componenteParaBorrar)
                    cargarDatosYActualizar(categoriaFiltro)
                }
            }
        }
    }
}