package com.rahul.assessmentankit.data.remote

import com.google.gson.annotations.SerializedName
import com.rahul.assessmentankit.data.local.ProductEntity
import com.rahul.assessmentankit.domain.model.Product

data class ProductResponseDto(
    @SerializedName("products") val products: List<ProductDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("skip") val skip: Int,
    @SerializedName("limit") val limit: Int
)

data class ProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("price") val price: Double,
    @SerializedName("discountPercentage") val discountPercentage: Double = 0.0,
    @SerializedName("rating") val rating: Double = 0.0,
    @SerializedName("stock") val stock: Int = 0,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("images") val images: List<String>? = emptyList()
) {
    fun toDomainModel(): Product {
        return Product(
            id = id,
            title = title,
            description = description,
            category = category,
            price = price,
            discountPercentage = discountPercentage,
            rating = rating,
            stock = stock,
            brand = brand ?: "Generic",
            thumbnail = thumbnail,
            images = images ?: listOf(thumbnail)
        )
    }

    fun toEntity(gson: com.google.gson.Gson): ProductEntity {
        return ProductEntity(
            id = id,
            title = title,
            description = description,
            category = category,
            price = price,
            discountPercentage = discountPercentage,
            rating = rating,
            stock = stock,
            brand = brand ?: "Generic",
            thumbnail = thumbnail,
            imagesJson = gson.toJson(images ?: listOf(thumbnail))
        )
    }
}

data class CategoryDto(
    @SerializedName("slug") val slug: String,
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String? = null
)
