package com.example.proyectopersonal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos_online")
data class ProductoOnline(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val brand: String? = null,
    val thumbnail: String? = null,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val discountPercentage: Double = 0.0
)
