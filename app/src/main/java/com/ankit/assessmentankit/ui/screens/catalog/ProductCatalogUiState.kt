package com.ankit.assessmentankit.ui.screens.catalog

import com.ankit.assessmentankit.domain.model.Category
import com.ankit.assessmentankit.domain.model.Product

sealed class CatalogUiState {
    object Loading : CatalogUiState()
    data class Success(val products: List<Product>) : CatalogUiState()
    object Empty : CatalogUiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : CatalogUiState()
}

data class ProductCatalogState(
    val uiState: CatalogUiState = CatalogUiState.Loading,
    val categories: List<Category> = emptyList(),
    val selectedCategorySlug: String? = null,
    val searchQuery: String = "",
    val cartItemCount: Int = 0,
    val isOffline: Boolean = false,
    val userNotification: String? = null
)
