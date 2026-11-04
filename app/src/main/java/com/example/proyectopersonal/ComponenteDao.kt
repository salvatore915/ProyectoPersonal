package com.example.proyectopersonal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ComponenteDao {
    @Insert
    suspend fun insertar(componente: Componente)

    @Query("SELECT * FROM componentes ORDER BY id DESC")
    suspend fun obtenerTodos(): List<Componente>

    @Delete
    suspend fun borrar(componente: Componente)

    @Update
    suspend fun actualizar(componente: Componente)

    @Query("SELECT COUNT(*) FROM componentes")
    suspend fun contarComponentes(): Int

    @Query("SELECT * FROM componentes WHERE categoria = :cat ORDER BY id DESC")
    suspend fun obtenerPorCategoria(cat: String): List<Componente>
}