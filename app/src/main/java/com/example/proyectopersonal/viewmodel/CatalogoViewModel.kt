package com.example.proyectopersonal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectopersonal.ProductoOnline
import com.example.proyectopersonal.repository.CatalogoRepository
import com.example.proyectopersonal.repository.ResultadoCatalogo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CatalogoUiState {
    object Loading : CatalogoUiState()
    data class Success(
        val productos: List<ProductoOnline>,
        val esOffline: Boolean,
        val mensajeAviso: String? = null
    ) : CatalogoUiState()
    data class Error(val mensaje: String) : CatalogoUiState()
}

class CatalogoViewModel(private val repository: CatalogoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogoUiState>(CatalogoUiState.Loading)
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    private var todosLosProductos: List<ProductoOnline> = emptyList()

    init {
        cargarProductos()
    }

    fun cargarProductos(forzarRed: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = CatalogoUiState.Loading
            when (val resultado = repository.obtenerProductos(forzarRed)) {
                is ResultadoCatalogo.Exito -> {
                    todosLosProductos = resultado.productos
                    _uiState.value = CatalogoUiState.Success(
                        productos = resultado.productos,
                        esOffline = resultado.esDesdeCache,
                        mensajeAviso = if (resultado.esDesdeCache) "Modo sin conexión: mostrando catálogo guardado en Room." else null
                    )
                }
                is ResultadoCatalogo.Error -> {
                    _uiState.value = CatalogoUiState.Error(resultado.mensaje)
                }
            }
        }
    }

    fun buscar(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.value = CatalogoUiState.Success(
                    productos = todosLosProductos,
                    esOffline = false
                )
                return@launch
            }

            _uiState.value = CatalogoUiState.Loading
            when (val resultado = repository.buscarProductos(query)) {
                is ResultadoCatalogo.Exito -> {
                    _uiState.value = CatalogoUiState.Success(
                        productos = resultado.productos,
                        esOffline = resultado.esDesdeCache,
                        mensajeAviso = if (resultado.esDesdeCache) "Búsqueda en datos locales (sin conexión)" else null
                    )
                }
                is ResultadoCatalogo.Error -> {
                    _uiState.value = CatalogoUiState.Error(resultado.mensaje)
                }
            }
        }
    }

    fun filtrarPorCategoria(categoria: String) {
        if (categoria == "Todos") {
            _uiState.value = CatalogoUiState.Success(
                productos = todosLosProductos,
                esOffline = false
            )
            return
        }

        val filtrados = todosLosProductos.filter {
            it.category.contains(categoria, ignoreCase = true)
        }
        _uiState.value = CatalogoUiState.Success(
            productos = filtrados,
            esOffline = false
        )
    }

    fun agregarAlPresupuestoLocal(producto: ProductoOnline, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exito = repository.agregarAComponentesLocales(producto)
            onResultado(exito)
        }
    }
}

class CatalogoViewModelFactory(private val repository: CatalogoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CatalogoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CatalogoViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}
