package com.ankit.assessmentankit.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ankit.assessmentankit.data.remote.NetworkResult
import com.ankit.assessmentankit.data.repository.CartRepository
import com.ankit.assessmentankit.data.repository.ProductRepository
import com.ankit.assessmentankit.domain.model.Product
import com.ankit.assessmentankit.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ProductDetailUiState {
    object Loading : ProductDetailUiState()
    data class Success(val product: Product) : ProductDetailUiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : ProductDetailUiState()
}

data class ProductDetailState(
    val uiState: ProductDetailUiState = ProductDetailUiState.Loading,
    val selectedQuantity: Int = 1,
    val cartItemCount: Int = 0,
    val isOffline: Boolean = false,
    val userNotification: String? = null
)

class ProductDetailViewModel(
    private val productId: Int,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(ProductDetailState())
    val state: StateFlow<ProductDetailState> = _state.asStateFlow()

    init {
        observeCartCount()
        observeNetworkState()
        loadProductDetail()
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
                _state.update { it.copy(isOffline = !isOnline) }
            }
        }
    }

    fun loadProductDetail() {
        viewModelScope.launch {
            _state.update { it.copy(uiState = ProductDetailUiState.Loading) }
            when (val result = productRepository.getProductById(productId)) {
                is NetworkResult.Success -> {
                    _state.update { it.copy(uiState = ProductDetailUiState.Success(result.data)) }
                }
                is NetworkResult.Error -> {
                    _state.update { 
                        it.copy(
                            uiState = ProductDetailUiState.Error(
                                message = result.message,
                                isNetworkError = result.isNetworkError
                            )
                        ) 
                    }
                }
                is NetworkResult.Loading -> {
                    _state.update { it.copy(uiState = ProductDetailUiState.Loading) }
                }
            }
        }
    }

    fun increaseQuantity() {
        val current = _state.value.selectedQuantity
        val product = (_state.value.uiState as? ProductDetailUiState.Success)?.product
        val maxStock = product?.stock ?: 99
        if (current < maxStock) {
            _state.update { it.copy(selectedQuantity = current + 1) }
        }
    }

    fun decreaseQuantity() {
        val current = _state.value.selectedQuantity
        if (current > 1) {
            _state.update { it.copy(selectedQuantity = current - 1) }
        }
    }

    fun addToCart() {
        viewModelScope.launch {
            val currentState = _state.value
            val product = (currentState.uiState as? ProductDetailUiState.Success)?.product ?: return@launch
            val qty = currentState.selectedQuantity

            cartRepository.addToCart(product, qty)
            _state.update { it.copy(userNotification = "Added $qty x '${product.title}' to cart") }
        }
    }

    fun clearNotification() {
        _state.update { it.copy(userNotification = null) }
    }

    class Factory(
        private val productId: Int,
        private val productRepository: ProductRepository,
        private val cartRepository: CartRepository,
        private val networkMonitor: NetworkMonitor
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductDetailViewModel(productId, productRepository, cartRepository, networkMonitor) as T
        }
    }
}
