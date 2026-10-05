package com.ankit.assessmentankit

import com.ankit.assessmentankit.data.remote.ProductDto
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductDtoTest {

    @Test
    fun productDto_toDomainModel_mapsAllFieldsCorrectly() {
        val dto = ProductDto(
            id = 42,
            title = "Essence Mascara",
            description = "Lash Princess mascara",
            category = "beauty",
            price = 9.99,
            discountPercentage = 5.0,
            rating = 4.8,
            stock = 10,
            brand = "Essence",
            thumbnail = "https://example.com/mascara.png",
            images = listOf("https://example.com/mascara_1.png", "https://example.com/mascara_2.png")
        )

        val domainModel = dto.toDomainModel()

        assertEquals(42, domainModel.id)
        assertEquals("Essence Mascara", domainModel.title)
        assertEquals("beauty", domainModel.category)
        assertEquals(9.99, domainModel.price, 0.001)
        assertEquals("Essence", domainModel.brand)
        assertEquals(2, domainModel.images.size)
    }

    @Test
    fun productDto_toDomainModel_handlesNullBrandAndImages() {
        val dto = ProductDto(
            id = 100,
            title = "Generic Item",
            description = "No brand item",
            category = "groceries",
            price = 2.50,
            thumbnail = "https://example.com/item.png",
            brand = null,
            images = null
        )

        val domainModel = dto.toDomainModel()

        assertEquals("Generic", domainModel.brand)
        assertEquals(1, domainModel.images.size)
        assertEquals("https://example.com/item.png", domainModel.images.first())
    }
}
