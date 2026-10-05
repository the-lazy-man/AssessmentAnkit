package com.ankit.assessmentankit.ui.screens.cart

import com.ankit.assessmentankit.domain.model.CartItem

data class CartState(
    val items: List<CartItem> = emptyList(),
    val totalItemCount: Int = 0,
    val totalPrice: Double = 0.0,
    val isOffline: Boolean = false,
    val isCheckoutDialogVisible: Boolean = false,
    val userNotification: String? = null
) {
    val formattedTotalPrice: String
        get() = String.format("$%.2f", totalPrice)
}
