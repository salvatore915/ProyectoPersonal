package com.example.proyectopersonal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectopersonal.network.RetrofitClient
import com.example.proyectopersonal.repository.CatalogoRepository
import com.example.proyectopersonal.viewmodel.CatalogoUiState
import com.example.proyectopersonal.viewmodel.CatalogoViewModel
import com.example.proyectopersonal.viewmodel.CatalogoViewModelFactory
import kotlinx.coroutines.launch

// Actividad que muestra el catálogo de productos y gestiona búsqueda, filtros y estados de pantalla
class CatalogoActivity : AppCompatActivity() {

    // Crea y retiene el ViewModel inyectando el repositorio con Retrofit y los DAOs de Room
    private val viewModel: CatalogoViewModel by viewModels {
        val db = AppDatabase.obtener(this)
        val repo = CatalogoRepository(
            api = RetrofitClient.api,
            productoDao = db.productoOnlineDao(),
            componenteDao = db.componenteDao()
        )
        CatalogoViewModelFactory(repo)
    }

    // Referencias a elementos de la interfaz y al adaptador de la lista
    private lateinit var adapter: CatalogoAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutError: LinearLayout
    private lateinit var tvError: TextView
    private lateinit var layoutOffline: LinearLayout
    private lateinit var tvOffline: TextView
    private lateinit var recycler: RecyclerView
    private lateinit var editBusqueda: EditText

    // Configura la vista, el RecyclerView y los listeners de botones y búsqueda
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogo)

        // Obtiene los botones de navegación, búsqueda, reintento y filtros por categoría
        val btnVolver = findViewById<Button>(R.id.btnVolverCatalogo)
        val btnBuscar = findViewById<Button>(R.id.btnBuscarCatalogo)
        val btnReintentar = findViewById<Button>(R.id.btnReintentar)
        val chipTodos = findViewById<Button>(R.id.chipTodos)
        val chipLaptops = findViewById<Button>(R.id.chipLaptops)
        val chipAccesorios = findViewById<Button>(R.id.chipAccesorios)

        // Obtiene los contenedores de estado y el campo de texto de búsqueda
        editBusqueda = findViewById(R.id.editBusquedaCatalogo)
        progressBar = findViewById(R.id.progressBarCatalogo)
        layoutError = findViewById(R.id.layoutErrorCatalogo)
        tvError = findViewById(R.id.tvMensajeError)
        layoutOffline = findViewById(R.id.layoutAvisoOffline)
        tvOffline = findViewById(R.id.tvAvisoOffline)
        recycler = findViewById(R.id.recyclerCatalogo)

        // Configura el diseño vertical y el adaptador que abre la pantalla de detalle al pulsar un producto
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = CatalogoAdapter(emptyList()) { producto ->
            val intent = Intent(this, DetalleProductoActivity::class.java)
            intent.putExtra("PRODUCTO_ID", producto.id)
            startActivity(intent)
        }
        recycler.adapter = adapter

        // Cierra la actividad actual y regresa a la pantalla anterior
        btnVolver.setOnClickListener {
            finish()
        }

        // Ejecuta la búsqueda con el texto introducido al pulsar el botón buscar
        btnBuscar.setOnClickListener {
            val query = editBusqueda.text.toString().trim()
            viewModel.buscar(query)
        }

        // Ejecuta la búsqueda al pulsar la tecla de buscar en el teclado del dispositivo
        editBusqueda.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = editBusqueda.text.toString().trim()
                viewModel.buscar(query)
                true
            } else {
                false
            }
        }

        // Fuerza una nueva consulta a red al pulsar reintentar
        btnReintentar.setOnClickListener {
            viewModel.cargarProductos(forzarRed = true)
        }

        // Filtra por todos los productos y actualiza el estilo visual del filtro activo
        chipTodos.setOnClickListener {
            marcarChipActivo(chipTodos, listOf(chipLaptops, chipAccesorios))
            viewModel.filtrarPorCategoria("Todos")
        }

        // Filtra por portátiles y marca el botón correspondiente como activo
        chipLaptops.setOnClickListener {
            marcarChipActivo(chipLaptops, listOf(chipTodos, chipAccesorios))
            viewModel.filtrarPorCategoria("Portátiles")
        }

        // Filtra por accesorios y marca el botón correspondiente como activo
        chipAccesorios.setOnClickListener {
            marcarChipActivo(chipAccesorios, listOf(chipTodos, chipLaptops))
            viewModel.filtrarPorCategoria("Accesorios")
        }

        // Inicia la observación de los estados emitidos por el ViewModel
        observarEstado()
    }

    // Cambia el fondo y color de texto para resaltar el botón seleccionado y atenuar el resto
    private fun marcarChipActivo(activo: Button, inactivos: List<Button>) {
        activo.setBackgroundResource(R.drawable.fondo_boton_principal)
        activo.setTextColor(resources.getColor(android.R.color.white, null))

        for (btn in inactivos) {
            btn.setBackgroundResource(R.drawable.fondo_boton_secundario)
            btn.setTextColor(resources.getColor(R.color.pc_texto, null))
        }
    }

    // Escucha uiState de forma segura respecto al ciclo de vida y actualiza la interfaz
    private fun observarEstado() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        // Muestra carga y oculta lista y error
                        is CatalogoUiState.Loading -> {
                            progressBar.visibility = View.VISIBLE
                            layoutError.visibility = View.GONE
                            recycler.visibility = View.GONE
                        }
                        // Muestra los productos y el aviso sin conexión si aplica
                        is CatalogoUiState.Success -> {
                            progressBar.visibility = View.GONE
                            layoutError.visibility = View.GONE
                            recycler.visibility = View.VISIBLE

                            adapter.actualizarLista(state.productos)

                            if (state.esOffline) {
                                layoutOffline.visibility = View.VISIBLE
                                tvOffline.text = state.mensajeAviso ?: "⚠️ Sin conexión a Internet. Mostrando datos guardados en Room."
                            } else {
                                layoutOffline.visibility = View.GONE
                            }
                        }
                        // Muestra el bloque de error y oculta carga, lista y aviso offline
                        is CatalogoUiState.Error -> {
                            progressBar.visibility = View.GONE
                            recycler.visibility = View.GONE
                            layoutOffline.visibility = View.GONE
                            layoutError.visibility = View.VISIBLE
                            tvError.text = state.mensaje
                        }
                    }
                }
            }
        }
    }
}