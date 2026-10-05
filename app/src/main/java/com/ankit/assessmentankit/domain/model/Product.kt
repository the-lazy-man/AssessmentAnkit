package com.ankit.assessmentankit.domain.model

data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    val brand: String?,
    val thumbnail: String,
    val images: List<String>
) {
    val discountedPrice: Double
        get() = price * (1 - discountPercentage / 100)

    val formattedPrice: String
        get() = String.format("$%.2f", price)

    val formattedDiscountedPrice: String
        get() = String.format("$%.2f", discountedPrice)
}

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val totalPrice: Double
        get() = product.price * quantity

    val formattedTotalPrice: String
        get() = String.format("$%.2f", totalPrice)
}

data class Category(
    val slug: String,
    val name: String
)
