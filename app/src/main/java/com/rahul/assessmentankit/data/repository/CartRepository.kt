package com.rahul.assessmentankit.data.repository

import com.google.gson.Gson
import com.rahul.assessmentankit.data.local.CartDao
import com.rahul.assessmentankit.data.local.CartItemEntity
import com.rahul.assessmentankit.domain.model.CartItem
import com.rahul.assessmentankit.domain.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CartRepository(
    private val cartDao: CartDao,
    private val gson: Gson
) {

    val cartItems: Flow<List<CartItem>> = cartDao.getCartItems().map { entities ->
        entities.map { it.toDomainModel(gson) }
    }

    val totalItemCount: Flow<Int> = cartItems.map { items ->
        items.sumOf { it.quantity }
    }

    val totalPrice: Flow<Double> = cartItems.map { items ->
        items.sumOf { it.totalPrice }
    }

    suspend fun addToCart(product: Product, quantityToAdd: Int = 1) = withContext(Dispatchers.IO) {
        val existing = cartDao.getCartItemByProductId(product.id)
        val newQuantity = if (existing != null) {
            existing.quantity + quantityToAdd
        } else {
            quantityToAdd
        }

        val entity = CartItemEntity(
            productId = product.id,
            title = product.title,
            description = product.description,
            category = product.category,
            price = product.price,
            discountPercentage = product.discountPercentage,
            rating = product.rating,
            stock = product.stock,
            brand = product.brand,
            thumbnail = product.thumbnail,
            imagesJson = gson.toJson(product.images),
            quantity = newQuantity,
            updatedAt = System.currentTimeMillis()
        )
        cartDao.insertOrUpdateCartItem(entity)
    }

    suspend fun updateQuantity(productId: Int, newQuantity: Int) = withContext(Dispatchers.IO) {
        if (newQuantity <= 0) {
            cartDao.deleteCartItem(productId)
        } else {
            cartDao.updateQuantity(productId, newQuantity)
        }
    }

    suspend fun removeCartItem(productId: Int) = withContext(Dispatchers.IO) {
        cartDao.deleteCartItem(productId)
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clearCart()
    }

    private fun CartItemEntity.toDomainModel(gson: Gson): CartItem {
        val imageList: List<String> = try {
            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            gson.fromJson(imagesJson, type) ?: listOf(thumbnail)
        } catch (e: Exception) {
            listOf(thumbnail)
        }
        val product = Product(
            id = productId,
            title = title,
            description = description,
            category = category,
            price = price,
            discountPercentage = discountPercentage,
            rating = rating,
            stock = stock,
            brand = brand,
            thumbnail = thumbnail,
            images = imageList
        )
        return CartItem(product = product, quantity = quantity)
    }
}
