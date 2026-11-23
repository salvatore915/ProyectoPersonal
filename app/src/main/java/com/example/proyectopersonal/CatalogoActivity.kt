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

class CatalogoActivity : AppCompatActivity() {

    private val viewModel: CatalogoViewModel by viewModels {
        val db = AppDatabase.obtener(this)
        val repo = CatalogoRepository(
            api = RetrofitClient.api,
            productoDao = db.productoOnlineDao(),
            componenteDao = db.componenteDao()
        )
        CatalogoViewModelFactory(repo)
    }

    private lateinit var adapter: CatalogoAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutError: LinearLayout
    private lateinit var tvError: TextView
    private lateinit var layoutOffline: LinearLayout
    private lateinit var tvOffline: TextView
    private lateinit var recycler: RecyclerView
    private lateinit var editBusqueda: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogo)

        val btnVolver = findViewById<Button>(R.id.btnVolverCatalogo)
        val btnBuscar = findViewById<Button>(R.id.btnBuscarCatalogo)
        val btnReintentar = findViewById<Button>(R.id.btnReintentar)
        val chipTodos = findViewById<Button>(R.id.chipTodos)
        val chipLaptops = findViewById<Button>(R.id.chipLaptops)
        val chipAccesorios = findViewById<Button>(R.id.chipAccesorios)

        editBusqueda = findViewById(R.id.editBusquedaCatalogo)
        progressBar = findViewById(R.id.progressBarCatalogo)
        layoutError = findViewById(R.id.layoutErrorCatalogo)
        tvError = findViewById(R.id.tvMensajeError)
        layoutOffline = findViewById(R.id.layoutAvisoOffline)
        tvOffline = findViewById(R.id.tvAvisoOffline)
        recycler = findViewById(R.id.recyclerCatalogo)

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = CatalogoAdapter(emptyList()) { producto ->
            val intent = Intent(this, DetalleProductoActivity::class.java)
            intent.putExtra("PRODUCTO_ID", producto.id)
            startActivity(intent)
        }
        recycler.adapter = adapter

        btnVolver.setOnClickListener {
            finish()
        }

        btnBuscar.setOnClickListener {
            val query = editBusqueda.text.toString().trim()
            viewModel.buscar(query)
        }

        editBusqueda.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = editBusqueda.text.toString().trim()
                viewModel.buscar(query)
                true
            } else {
                false
            }
        }

        btnReintentar.setOnClickListener {
            viewModel.cargarProductos(forzarRed = true)
        }

        chipTodos.setOnClickListener {
            marcarChipActivo(chipTodos, listOf(chipLaptops, chipAccesorios))
            viewModel.filtrarPorCategoria("Todos")
        }

        chipLaptops.setOnClickListener {
            marcarChipActivo(chipLaptops, listOf(chipTodos, chipAccesorios))
            viewModel.filtrarPorCategoria("Portátiles")
        }

        chipAccesorios.setOnClickListener {
            marcarChipActivo(chipAccesorios, listOf(chipTodos, chipLaptops))
            viewModel.filtrarPorCategoria("Accesorios")
        }

        observarEstado()
    }

    private fun marcarChipActivo(activo: Button, inactivos: List<Button>) {
        activo.setBackgroundResource(R.drawable.fondo_boton_principal)
        activo.setTextColor(resources.getColor(android.R.color.white, null))

        for (btn in inactivos) {
            btn.setBackgroundResource(R.drawable.fondo_boton_secundario)
            btn.setTextColor(resources.getColor(R.color.pc_texto, null))
        }
    }

    private fun observarEstado() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is CatalogoUiState.Loading -> {
                            progressBar.visibility = View.VISIBLE
                            layoutError.visibility = View.GONE
                            recycler.visibility = View.GONE
                        }
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
