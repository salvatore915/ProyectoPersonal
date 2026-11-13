package com.example.proyectopersonal.repository

import com.example.proyectopersonal.Componente
import com.example.proyectopersonal.ComponenteDao
import com.example.proyectopersonal.ProductoOnline
import com.example.proyectopersonal.ProductoOnlineDao
import com.example.proyectopersonal.network.DummyJsonApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class ResultadoCatalogo {
    data class Exito(val productos: List<ProductoOnline>, val esDesdeCache: Boolean) : ResultadoCatalogo()
    data class Error(val mensaje: String, val productosCache: List<ProductoOnline>? = null) : ResultadoCatalogo()
}

class CatalogoRepository(
    private val api: DummyJsonApi,
    private val productoDao: ProductoOnlineDao,
    private val componenteDao: ComponenteDao
) {

    suspend fun obtenerProductos(forzarRed: Boolean = false): ResultadoCatalogo = withContext(Dispatchers.IO) {
        try {
            // Obtenemos laptops y accesorios relacionados con computación y tecnología
            val laptopsResp = api.getLaptops()
            val accesoriosResp = try {
                api.getAccesorios()
            } catch (e: Exception) {
                null
            }

            val listaLaptops = laptopsResp.products.map { it.toEntity() }
            val listaAccesorios = accesoriosResp?.products?.map { it.toEntity() } ?: emptyList()
            val listaCompleta = listaLaptops + listaAccesorios

            if (listaCompleta.isNotEmpty()) {
                // Guardamos en Room para disponibilidad sin conexión
                productoDao.insertarTodos(listaCompleta)
            }

            ResultadoCatalogo.Exito(productos = listaCompleta, esDesdeCache = false)
        } catch (e: Exception) {
            // Ante fallo de conexión, consultamos Room
            val cache = try {
                productoDao.obtenerTodos()
            } catch (dbEx: Exception) {
                emptyList()
            }

            if (cache.isNotEmpty()) {
                ResultadoCatalogo.Exito(productos = cache, esDesdeCache = true)
            } else {
                ResultadoCatalogo.Error(
                    mensaje = "No se pudo conectar a Internet y no hay datos guardados previamente.",
                    productosCache = null
                )
            }
        }
    }

    suspend fun buscarProductos(query: String): ResultadoCatalogo = withContext(Dispatchers.IO) {
        val queryLimpio = query.trim()
        if (queryLimpio.isEmpty()) {
            return@withContext obtenerProductos()
        }

        try {
            val response = api.searchProducts(queryLimpio)
            val productos = response.products.map { it.toEntity() }
            if (productos.isNotEmpty()) {
                productoDao.insertarTodos(productos)
            }
            ResultadoCatalogo.Exito(productos = productos, esDesdeCache = false)
        } catch (e: Exception) {
            // Fallback de búsqueda en base de datos local
            val cache = try {
                productoDao.buscar(queryLimpio)
            } catch (dbEx: Exception) {
                emptyList()
            }

            if (cache.isNotEmpty()) {
                ResultadoCatalogo.Exito(productos = cache, esDesdeCache = true)
            } else {
                ResultadoCatalogo.Error(
                    mensaje = "Sin conexión. No se encontraron resultados guardados para '$queryLimpio'.",
                    productosCache = null
                )
            }
        }
    }

    suspend fun filtrarPorCategoria(categoria: String): ResultadoCatalogo = withContext(Dispatchers.IO) {
        if (categoria == "Todos" || categoria.isEmpty()) {
            return@withContext obtenerProductos()
        }

        try {
            val cache = productoDao.obtenerPorCategoria(categoria.lowercase())
            if (cache.isNotEmpty()) {
                return@withContext ResultadoCatalogo.Exito(productos = cache, esDesdeCache = false)
            }
            obtenerProductos()
        } catch (e: Exception) {
            obtenerProductos()
        }
    }

    suspend fun obtenerDetalle(id: Int): ProductoOnline? = withContext(Dispatchers.IO) {
        try {
            val dto = api.getProductById(id)
            val entidad = dto.toEntity()
            productoDao.insertar(entidad)
            entidad
        } catch (e: Exception) {
            productoDao.obtenerPorId(id)
        }
    }

    // Funcionalidad propia integrada: Añadir producto de la tienda online al presupuesto local de TORRECOMS
    suspend fun agregarAComponentesLocales(producto: ProductoOnline): Boolean = withContext(Dispatchers.IO) {
        try {
            val catMapeada = when {
                producto.category.contains("laptop", ignoreCase = true) -> "Otro"
                producto.title.contains("SSD", ignoreCase = true) || producto.title.contains("Disk", ignoreCase = true) -> "Disco"
                producto.title.contains("RAM", ignoreCase = true) || producto.title.contains("Memory", ignoreCase = true) -> "RAM"
                producto.title.contains("Processor", ignoreCase = true) || producto.title.contains("CPU", ignoreCase = true) -> "CPU"
                else -> "Otro"
            }

            val nuevoComponente = Componente(
                nombre = producto.title,
                precio = producto.price,
                categoria = catMapeada,
                latitud = null,
                longitud = null,
                fotoPath = null
            )
            componenteDao.insertar(nuevoComponente)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
