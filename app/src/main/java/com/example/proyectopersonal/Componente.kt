package com.example.proyectopersonal

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entidad de Room que representa la tabla de componentes guardados en la base de datos local
@Entity(tableName = "componentes")
data class Componente(
    val nombre: String, // Nombre del componente de hardware
    val precio: Double, // Precio unitario del componente
    val categoria: String, // Categoría asignada como CPU, RAM, Disco u Otro
    val latitud: Double? = null, // Coordenada de latitud opcional
    val longitud: Double? = null, // Coordenada de longitud opcional
    val fotoPath: String? = null, // Ruta local de la imagen asociada si existe
    @PrimaryKey(autoGenerate = true) val id: Int = 0 // Clave primaria autogenerada por Room
)