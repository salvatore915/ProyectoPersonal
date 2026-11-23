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

// Estados posibles que la UI observará para renderizar carga, datos o error
sealed class CatalogoUiState {
    object Loading : CatalogoUiState()
    data class Success(
        val productos: List<ProductoOnline>,
        val esOffline: Boolean,
        val mensajeAviso: String? = null
    ) : CatalogoUiState()
    data class Error(val mensaje: String) : CatalogoUiState()
}

// ViewModel que gestiona el estado de la pantalla de catálogo y delega la obtención de datos al repositorio
class CatalogoViewModel(private val repository: CatalogoRepository) : ViewModel() {

    // Flujo mutable interno y versión inmutable expuesta hacia la vista
    private val _uiState = MutableStateFlow<CatalogoUiState>(CatalogoUiState.Loading)
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    // Copia completa en memoria para poder filtrar por categoría sin volver a consultar
    private var todosLosProductos: List<ProductoOnline> = emptyList()

    // Indica si la última carga provino de la caché local de Room
    private var cargadoDesdeCache = false

    // Devuelve un aviso si los datos actuales vienen de caché, o null si vienen de red
    private fun avisoCarga(): String? =
        if (cargadoDesdeCache) "Modo sin conexión: mostrando catálogo guardado en Room." else null

    // Carga inicial automática al crear el ViewModel
    init {
        cargarProductos()
    }

    // Solicita los productos al repositorio y actualiza `uiState` según el resultado
    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.value = CatalogoUiState.Loading
            when (val resultado = repository.obtenerProductos()) {
                is ResultadoCatalogo.Exito -> {
                    todosLosProductos = resultado.productos
                    cargadoDesdeCache = resultado.esDesdeCache
                    _uiState.value = CatalogoUiState.Success(
                        productos = resultado.productos,
                        esOffline = resultado.esDesdeCache,
                        mensajeAviso = avisoCarga()
                    )
                }
                is ResultadoCatalogo.Error -> {
                    _uiState.value = CatalogoUiState.Error(resultado.mensaje)
                }
            }
        }
    }

    // Busca productos por texto; si el texto está vacío, restaura la lista ya cargada
    fun buscar(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.value = CatalogoUiState.Success(
                    productos = todosLosProductos,
                    esOffline = cargadoDesdeCache,
                    mensajeAviso = avisoCarga()
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

    // Filtra en memoria los productos cargados según la categoría seleccionada
    fun filtrarPorCategoria(categoria: String) {
        val lista = if (categoria == "Todos") {
            todosLosProductos
        } else {
            todosLosProductos.filter { it.category.contains(categoria, ignoreCase = true) }
        }
        _uiState.value = CatalogoUiState.Success(
            productos = lista,
            esOffline = cargadoDesdeCache,
            mensajeAviso = avisoCarga()
        )
    }
}

// Factory necesaria para instanciar CatalogoViewModel pasando `repository` por constructor
class CatalogoViewModelFactory(private val repository: CatalogoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CatalogoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CatalogoViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}