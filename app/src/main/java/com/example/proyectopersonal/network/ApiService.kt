package com.example.proyectopersonal.network

import com.example.proyectopersonal.ProductoOnline
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Modelo que representa la respuesta JSON paginada al pedir un listado de productos
data class ProductoListResponse(
    @SerializedName("products") val products: List<ProductoDto>, // Lista de productos devuelta por la API
    @SerializedName("total") val total: Int, // Total de elementos disponibles en la API
    @SerializedName("skip") val skip: Int, // Número de elementos omitidos (paginación)
    @SerializedName("limit") val limit: Int // Máximo de elementos devueltos en esta respuesta
)

// DTO (Data Transfer Object) que mapea los campos JSON de un producto individual
data class ProductoDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double,
    @SerializedName("category") val category: String,
    @SerializedName("brand") val brand: String? = null, // Puede venir nulo en el JSON
    @SerializedName("thumbnail") val thumbnail: String? = null, // URL de la imagen miniatura (opcional)
    @SerializedName("rating") val rating: Double = 0.0,
    @SerializedName("stock") val stock: Int = 0,
    @SerializedName("discountPercentage") val discountPercentage: Double = 0.0
) {
    // Convierte el objeto de red (ProductoDto) al modelo de dominio/entidad usado en la app
    fun toEntity(): ProductoOnline {
        // Traduce categorías conocidas al español o capitaliza la primera letra por defecto
        val catTraducida = when (category.lowercase()) {
            "laptops" -> "Portátiles"
            "mobile-accessories" -> "Accesorios"
            else -> category.replaceFirstChar { it.uppercase() }
        }

        return ProductoOnline(
            id = id,
            title = title,
            description = description,
            price = price,
            category = catTraducida,
            brand = brand ?: "Genérico", // Asigna "Genérico" si la marca viene nula
            thumbnail = thumbnail,
            rating = rating,
            stock = stock,
            discountPercentage = discountPercentage
        )
    }
}

// Interfaz de Retrofit que define los endpoints HTTP de la API
interface ApiService {

    // Obtiene los productos de la categoría "laptops" de forma asíncrona (corutina)
    @GET("products/category/laptops")
    suspend fun getLaptops(): ProductoListResponse

    // Obtiene los productos de la categoría "mobile-accessories"
    @GET("products/category/mobile-accessories")
    suspend fun getAccesorios(): ProductoListResponse

    // Busca productos pasando el texto como parámetro de consulta (?q=...)
    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): ProductoListResponse

    // Obtiene un único producto sustituyendo {id} en la ruta
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): ProductoDto
}

// Singleton que configura y expone la instancia de Retrofit y el servicio de red
object RetrofitClient {
    private const val BASE_URL = "https://dummyjson.com/" // URL base del servidor

    // Se inicializa solo la primera vez que se accede a `api`
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Convierte JSON <-> data classes con Gson
            .build()
            .create(ApiService::class.java)
    }
}