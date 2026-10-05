package com.rahul.assessmentankit.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rahul.assessmentankit.data.repository.CartRepository
import com.rahul.assessmentankit.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CartViewModel(
    private val cartRepository: CartRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    init {
        observeCartData()
        observeNetworkState()
    }

    private fun observeCartData() {
        viewModelScope.launch {
            combine(
                cartRepository.cartItems,
                cartRepository.totalItemCount,
                cartRepository.totalPrice
            ) { items, count, price ->
                Triple(items, count, price)
            }.collect { (items, count, price) ->
                _state.update { 
                    it.copy(
                        items = items,
                        totalItemCount = count,
                        totalPrice = price
                    ) 
                }
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

    fun increaseQuantity(productId: Int, currentQuantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(productId, currentQuantity + 1)
        }
    }

    fun decreaseQuantity(productId: Int, currentQuantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(productId, currentQuantity - 1)
        }
    }

    fun removeItem(productId: Int) {
        viewModelScope.launch {
            cartRepository.removeCartItem(productId)
            _state.update { it.copy(userNotification = "Item removed from cart") }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
            _state.update { it.copy(userNotification = "Cart cleared") }
        }
    }

    fun onCheckoutClick() {
        _state.update { it.copy(isCheckoutDialogVisible = true) }
    }

    fun dismissCheckoutDialog() {
        _state.update { it.copy(isCheckoutDialogVisible = false) }
    }

    fun confirmCheckout() {
        viewModelScope.launch {
            cartRepository.clearCart()
            _state.update { 
                it.copy(
                    isCheckoutDialogVisible = false,
                    userNotification = "Order placed successfully!"
                ) 
            }
        }
    }

    fun clearNotification() {
        _state.update { it.copy(userNotification = null) }
    }

    class Factory(
        private val cartRepository: CartRepository,
        private val networkMonitor: NetworkMonitor
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CartViewModel(cartRepository, networkMonitor) as T
        }
    }
}
