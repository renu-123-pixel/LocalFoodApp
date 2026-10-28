package com.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.local.CartDao
import com.data.local.toCartItem
import com.data.local.toEntity
import com.data.model.CartItem
import com.data.model.FoodItem
import com.data.model.Restaurant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartDao: CartDao
) : ViewModel() {

    val cartItems: StateFlow<List<CartItem>> = cartDao.getCartItems()
        .map { entities -> entities.map { it.toCartItem() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartTotal: StateFlow<Int> = cartItems.map { items ->
        items.sumOf { it.foodItem.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartCount: StateFlow<Int> = cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun addToCart(item: FoodItem, restaurant: Restaurant): Boolean {
        val current = cartItems.value
        if (current.isNotEmpty() && current.first().restaurantId != restaurant.id) {
            _message.value = "Replace cart with items from ${restaurant.name}?"
            return false
        }
        
        viewModelScope.launch {
            val existing = cartDao.getCartItemById(item.id)
            if (existing != null) {
                cartDao.updateCartItem(existing.copy(quantity = existing.quantity + 1))
            } else {
                cartDao.addToCart(CartItem(item, restaurant.id, restaurant.name).toEntity())
            }
            _message.value = "${item.name} added 🛒"
        }
        return true
    }

    fun removeFromCart(itemId: Int) {
        viewModelScope.launch {
            val existing = cartDao.getCartItemById(itemId) ?: return@launch
            if (existing.quantity > 1) {
                cartDao.updateCartItem(existing.copy(quantity = existing.quantity - 1))
            } else {
                cartDao.removeFromCart(existing)
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartDao.clearCart()
        }
    }

    fun clearCartAndAdd(item: FoodItem, restaurant: Restaurant) {
        viewModelScope.launch {
            cartDao.clearCart()
            cartDao.addToCart(CartItem(item, restaurant.id, restaurant.name).toEntity())
            _message.value = "${item.name} added 🛒"
        }
    }

    fun clearMessage() { _message.value = null }
}
