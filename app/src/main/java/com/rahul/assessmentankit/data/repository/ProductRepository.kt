package com.rahul.assessmentankit.data.repository

import com.google.gson.Gson
import com.rahul.assessmentankit.data.local.ProductDao
import com.rahul.assessmentankit.data.local.ProductEntity
import com.rahul.assessmentankit.data.remote.DummyJsonApi
import com.rahul.assessmentankit.data.remote.NetworkResult
import com.rahul.assessmentankit.domain.model.Category
import com.rahul.assessmentankit.domain.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

class ProductRepository(
    private val api: DummyJsonApi,
    private val productDao: ProductDao,
    private val gson: Gson
) {

    val cachedProducts: Flow<List<Product>> = productDao.getAllProducts().map { entities ->
        entities.map { it.toDomainModel(gson) }
    }

    suspend fun getProducts(forceRefresh: Boolean = false): NetworkResult<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProducts(limit = 100)
            val products = response.products.map { it.toDomainModel() }
            val entities = response.products.map { it.toEntity(gson) }
            
            // Save to local cache for offline access
            productDao.insertProducts(entities)
            
            NetworkResult.Success(products)
        } catch (e: IOException) {
            // Network failure - load offline cache
            val localProducts = productDao.getAllProducts().firstOrNull()?.map { it.toDomainModel(gson) }
            if (!localProducts.isNullOrEmpty()) {
                NetworkResult.Success(localProducts)
            } else {
                NetworkResult.Error("No internet connection and no cached products found.", cause = e, isNetworkError = true)
            }
        } catch (e: Exception) {
            val localProducts = productDao.getAllProducts().firstOrNull()?.map { it.toDomainModel(gson) }
            if (!localProducts.isNullOrEmpty()) {
                NetworkResult.Success(localProducts)
            } else {
                NetworkResult.Error(e.localizedMessage ?: "Failed to fetch products", cause = e)
            }
        }
    }

    suspend fun searchProducts(query: String): NetworkResult<List<Product>> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext getProducts()
        }
        try {
            val response = api.searchProducts(query = query, limit = 100)
            val products = response.products.map { it.toDomainModel() }
            NetworkResult.Success(products)
        } catch (e: IOException) {
            // Network error -> search local Room database
            val localResults = productDao.searchProducts(query).firstOrNull()?.map { it.toDomainModel(gson) }
            if (!localResults.isNullOrEmpty()) {
                NetworkResult.Success(localResults)
            } else {
                NetworkResult.Error("Offline mode: No matching products found locally.", cause = e, isNetworkError = true)
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Search failed", cause = e)
        }
    }

    suspend fun getProductById(id: Int): NetworkResult<Product> = withContext(Dispatchers.IO) {
        try {
            val dto = api.getProductById(id)
            NetworkResult.Success(dto.toDomainModel())
        } catch (e: Exception) {
            // Fallback to local Room database if offline
            val local = productDao.getProductById(id)
            if (local != null) {
                NetworkResult.Success(local.toDomainModel(gson))
            } else {
                NetworkResult.Error("Product not found", cause = e, isNetworkError = e is IOException)
            }
        }
    }

    suspend fun getCategories(): NetworkResult<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCategories()
            val categories = response.map { Category(slug = it.slug, name = it.name) }
            NetworkResult.Success(categories)
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Failed to fetch categories", cause = e, isNetworkError = e is IOException)
        }
    }

    suspend fun getProductsByCategory(categorySlug: String): NetworkResult<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProductsByCategory(categorySlug)
            val products = response.products.map { it.toDomainModel() }
            NetworkResult.Success(products)
        } catch (e: IOException) {
            val localCategoryProducts = productDao.getProductsByCategory(categorySlug).firstOrNull()?.map { it.toDomainModel(gson) }
            if (!localCategoryProducts.isNullOrEmpty()) {
                NetworkResult.Success(localCategoryProducts)
            } else {
                NetworkResult.Error("Offline mode: No products found for this category.", cause = e, isNetworkError = true)
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Failed to fetch category products", cause = e)
        }
    }

    private fun ProductEntity.toDomainModel(gson: Gson): Product {
        val imageList: List<String> = try {
            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            gson.fromJson(imagesJson, type) ?: listOf(thumbnail)
        } catch (e: Exception) {
            listOf(thumbnail)
        }
        return Product(
            id = id,
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
    }
}
