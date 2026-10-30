package com.example.proyectopersonal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ComponenteDao {
    @Insert
    suspend fun insertar(componente: Componente)

    @Query("SELECT * FROM componentes ORDER BY id DESC")
    suspend fun obtenerTodos(): List<Componente>

    @Delete
    suspend fun borrar(componente: Componente)
}