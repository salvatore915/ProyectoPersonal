package com.example.proyectopersonal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Componente::class, ProductoOnline::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun componenteDao(): ComponenteDao
    abstract fun productoOnlineDao(): ProductoOnlineDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                val nueva = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "componentes_db"
                ).fallbackToDestructiveMigration().build()
                instancia = nueva
                nueva
            }
        }
    }
}