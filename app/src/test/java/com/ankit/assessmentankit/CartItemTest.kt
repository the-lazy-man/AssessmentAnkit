package com.ankit.assessmentankit

import com.ankit.assessmentankit.domain.model.CartItem
import com.ankit.assessmentankit.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Test

class CartItemTest {

    private val sampleProduct = Product(
        id = 1,
        title = "Test Phone",
        description = "A test smartphone",
        category = "smartphones",
        price = 199.99,
        discountPercentage = 10.0,
        rating = 4.5,
        stock = 25,
        brand = "TestBrand",
        thumbnail = "https://example.com/thumb.jpg",
        images = listOf("https://example.com/img1.jpg")
    )

    @Test
    fun cartItem_totalPrice_calculatedCorrectly() {
        val cartItem = CartItem(product = sampleProduct, quantity = 3)
        
        // 199.99 * 3 = 599.97
        assertEquals(599.97, cartItem.totalPrice, 0.001)
        assertEquals("$599.97", cartItem.formattedTotalPrice)
    }

    @Test
    fun product_discountedPrice_calculatedCorrectly() {
        // 199.99 - 10% = 179.991
        assertEquals(179.991, sampleProduct.discountedPrice, 0.001)
        assertEquals("$199.99", sampleProduct.formattedPrice)
    }
}
