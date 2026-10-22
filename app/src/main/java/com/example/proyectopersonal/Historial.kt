package com.example.proyectopersonal

object Historial {
    val componentes = mutableListOf<Componente>()

    fun obtenerTotalPrecio(): Double {
        var total = 0.0
        for (componente in componentes) {
            total = total + componente.precio
        }
        return total
    }
}