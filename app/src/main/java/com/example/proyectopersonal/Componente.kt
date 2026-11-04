package com.example.proyectopersonal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "componentes")
data class Componente(
    val nombre: String,
    val precio: Double,
    val categoria: String,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoPath: String? = null,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)