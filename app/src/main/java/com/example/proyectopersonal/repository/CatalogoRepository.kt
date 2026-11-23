package com.example.proyectopersonal.repository

import com.example.proyectopersonal.Componente
import com.example.proyectopersonal.ComponenteDao
import com.example.proyectopersonal.ProductoOnline
import com.example.proyectopersonal.ProductoOnlineDao
import com.example.proyectopersonal.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Representa los posibles resultados al consultar el catálogo (éxito con datos de red/caché o error)
sealed class ResultadoCatalogo {
    data class Exito(val productos: List<ProductoOnline>, val esDesdeCache: Boolean) : ResultadoCatalogo()
    data class Error(val mensaje: String, val productosCache: List<ProductoOnline>? = null) : ResultadoCatalogo()
}

// Repositorio que centraliza el acceso a la API remota y a las tablas locales de Room
class CatalogoRepository(
    private val api: ApiService,
    private val productoDao: ProductoOnlineDao,
    private val componenteDao: ComponenteDao
) {

    // Obtiene portátiles y accesorios desde la API; si falla la red, recurre a la caché local de Room
    suspend fun obtenerProductos(): ResultadoCatalogo = withContext(Dispatchers.IO) {
        try {
            val laptopsResp = api.getLaptops()
            // Si falla la petición de accesorios, se ignora el error devolviendo null para no perder los portátiles
            val accesoriosResp = try {
                api.getAccesorios()
            } catch (e: Exception) {
                null
            }

            // Transforma los DTOs de red a entidades y combina ambas listas
            val listaLaptops = laptopsResp.products.map { it.toEntity() }
            val listaAccesorios = accesoriosResp?.products?.map { it.toEntity() } ?: emptyList()
            val listaCompleta = listaLaptops + listaAccesorios

            // Guarda en Room los productos descargados para tener caché sin conexión
            if (listaCompleta.isNotEmpty()) {
                productoDao.insertarTodos(listaCompleta)
            }

            ResultadoCatalogo.Exito(productos = listaCompleta, esDesdeCache = false)
        } catch (e: Exception) {
            // Intenta recuperar productos guardados previamente en la base de datos local
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

    // Busca productos por texto en la API o, si no hay red, en los registros locales de Room
    suspend fun buscarProductos(query: String): ResultadoCatalogo = withContext(Dispatchers.IO) {
        val queryLimpio = query.trim()
        // Si el buscador está vacío, carga el listado general
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
            // Busca coincidencias en la base de datos local cuando falla la conexión
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

    // Obtiene el detalle de un producto por ID desde la API y lo guarda en Room; si falla, lee de caché
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

    // Convierte un producto online en un Componente local y lo inserta en la tabla de componentes
    suspend fun agregarAComponentesLocales(
        producto: ProductoOnline,
        fotoPath: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Clasifica el producto según su categoría o palabras clave del título
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
                fotoPath = fotoPath
            )
            componenteDao.insertar(nuevoComponente)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}