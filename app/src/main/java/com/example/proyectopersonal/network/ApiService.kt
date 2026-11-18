package com.example.proyectopersonal.network

import com.example.proyectopersonal.ProductoOnline
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class ProductoListResponse(
    @SerializedName("products") val products: List<ProductoDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("skip") val skip: Int,
    @SerializedName("limit") val limit: Int
)

data class ProductoDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double,
    @SerializedName("category") val category: String,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("thumbnail") val thumbnail: String? = null,
    @SerializedName("rating") val rating: Double = 0.0,
    @SerializedName("stock") val stock: Int = 0,
    @SerializedName("discountPercentage") val discountPercentage: Double = 0.0
) {
    fun toEntity(): ProductoOnline {
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
            brand = brand ?: "Genérico",
            thumbnail = thumbnail,
            rating = rating,
            stock = stock,
            discountPercentage = discountPercentage
        )
    }
}

interface ApiService {

    @GET("products/category/laptops")
    suspend fun getLaptops(): ProductoListResponse

    @GET("products/category/mobile-accessories")
    suspend fun getAccesorios(): ProductoListResponse

    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): ProductoListResponse

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): ProductoDto
}

object RetrofitClient {
    private const val BASE_URL = "https://dummyjson.com/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}