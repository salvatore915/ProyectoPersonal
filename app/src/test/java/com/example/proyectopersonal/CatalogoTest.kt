package com.example.proyectopersonal

import com.example.proyectopersonal.network.ProductoDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CatalogoTest {

    @Test
    fun dtoToEntity_conversionCorrecta() {
        val dto = ProductoDto(
            id = 1,
            title = "MacBook Pro",
            description = "Potente ordenador portátil",
            price = 1999.99,
            category = "laptops",
            brand = "Apple",
            thumbnail = "https://cdn.dummyjson.com/img.png",
            rating = 4.8,
            stock = 15,
            discountPercentage = 5.0
        )

        val entidad = dto.toEntity()

        assertEquals(1, entidad.id)
        assertEquals("MacBook Pro", entidad.title)
        assertEquals(1999.99, entidad.price, 0.001)
        assertEquals("Apple", entidad.brand)
        assertEquals("Portátiles", entidad.category)
        assertNotNull(entidad.thumbnail)
    }

    @Test
    fun formatoPrecio_calculaCorrectamente() {
        val precio = 1250.5
        val textoEsperado = "1250.50 €"
        val formateado = String.format(java.util.Locale.US, "%.2f €", precio)
        assertEquals(textoEsperado, formateado)
    }
}
