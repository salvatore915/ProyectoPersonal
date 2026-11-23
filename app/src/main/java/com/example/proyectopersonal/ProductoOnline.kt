package com.example.proyectopersonal

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entidad de Room que representa la tabla de productos descargados del catálogo online
@Entity(tableName = "productos_online")
data class ProductoOnline(
    @PrimaryKey val id: Int, // Identificador único del producto procedente de la API
    val title: String, // Nombre o título del producto
    val description: String, // Descripción detallada del producto
    val price: Double, // Precio del producto
    val category: String, // Categoría ya formateada para la aplicación
    val brand: String? = null, // Marca del producto o valor por defecto si viene nula
    val thumbnail: String? = null, // Enlace a la imagen miniatura del producto
    val rating: Double = 0.0, // Valoración media del producto
    val stock: Int = 0, // Unidades disponibles en inventario
    val discountPercentage: Double = 0.0 // Porcentaje de descuento aplicado
)