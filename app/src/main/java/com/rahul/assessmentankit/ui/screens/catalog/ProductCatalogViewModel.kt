package com.rahul.assessmentankit.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rahul.assessmentankit.data.remote.NetworkResult
import com.rahul.assessmentankit.data.repository.CartRepository
import com.rahul.assessmentankit.data.repository.ProductRepository
import com.rahul.assessmentankit.domain.model.Product
import com.rahul.assessmentankit.util.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductCatalogViewModel(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(ProductCatalogState())
    val state: StateFlow<ProductCatalogState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        observeCartCount()
        observeNetworkState()
        loadCategories()
        loadProducts()
    }

    private fun observeCartCount() {
        viewModelScope.launch {
            cartRepository.totalItemCount.collect { count ->
                _state.update { it.copy(cartItemCount = count) }
            }
        }
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                val wasOffline = _state.value.isOffline
                _state.update { it.copy(isOffline = !isOnline) }
                // Re-trigger load if connection restored
                if (wasOffline && isOnline && _state.value.uiState is CatalogUiState.Error) {
                    loadProducts()
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = productRepository.getCategories()) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(categories = result.data) }
                }
                else -> { /* Ignore category load failure gracefully */ }
            }
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            _state.update { it.copy(uiState = CatalogUiState.Loading) }

            val result = if (_state.value.selectedCategorySlug != null) {
                productRepository.getProductsByCategory(_state.value.selectedCategorySlug!!)
            } else if (_state.value.searchQuery.isNotBlank()) {
                productRepository.searchProducts(_state.value.searchQuery)
            } else {
                productRepository.getProducts()
            }

            handleProductResult(result)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) // Debounce 400ms
            if (query.isBlank()) {
                loadProducts()
            } else {
                _state.update { it.copy(uiState = CatalogUiState.Loading) }
                val result = productRepository.searchProducts(query)
                handleProductResult(result)
            }
        }
    }

    fun onCategorySelected(slug: String?) {
        _state.update { 
            it.copy(
                selectedCategorySlug = slug,
                searchQuery = "" // Reset search when category filter is clicked
            ) 
        }
        loadProducts()
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            cartRepository.addToCart(product, 1)
            showNotification("'${product.title}' added to cart")
        }
    }

    fun clearNotification() {
        _state.update { it.copy(userNotification = null) }
    }

    private fun showNotification(msg: String) {
        _state.update { it.copy(userNotification = msg) }
    }

    private fun handleProductResult(result: NetworkResult<List<Product>>) {
        when (result) {
            is NetworkResult.Success -> {
                if (result.data.isEmpty()) {
                    _state.update { it.copy(uiState = CatalogUiState.Empty) }
                } else {
                    _state.update { it.copy(uiState = CatalogUiState.Success(result.data)) }
                }
            }
            is NetworkResult.Error -> {
                _state.update { 
                    it.copy(
                        uiState = CatalogUiState.Error(
                            message = result.message,
                            isNetworkError = result.isNetworkError
                        )
                    ) 
                }
            }
            is NetworkResult.Loading -> {
                _state.update { it.copy(uiState = CatalogUiState.Loading) }
            }
        }
    }

    class Factory(
        private val productRepository: ProductRepository,
        private val cartRepository: CartRepository,
        private val networkMonitor: NetworkMonitor
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductCatalogViewModel(productRepository, cartRepository, networkMonitor) as T
        }
    }
}
