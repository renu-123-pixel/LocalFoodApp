package com.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.data.model.PartnerMenuItem
import com.data.model.RegisteredRestaurant
import com.data.model.RegistrationStatus
import com.data.repository.RestaurantPartnerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PartnerViewModel @Inject constructor(
    application: Application,
    private val repository: RestaurantPartnerRepository
) : AndroidViewModel(application) {

    // Logged-in partner restaurant
    private val _loggedInRestaurant = MutableStateFlow<RegisteredRestaurant?>(null)
    val loggedInRestaurant: StateFlow<RegisteredRestaurant?> = _loggedInRestaurant.asStateFlow()

    // Registration step (1-4)
    private val _registrationStep = MutableStateFlow(1)
    val registrationStep: StateFlow<Int> = _registrationStep.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Snackbar
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // Add menu item dialog
    private val _showAddItemDialog = MutableStateFlow(false)
    val showAddItemDialog: StateFlow<Boolean> = _showAddItemDialog.asStateFlow()

    // All restaurants list (Source: Firestore)
    val allRestaurants: StateFlow<List<RegisteredRestaurant>> = repository.getRegisteredRestaurants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Login ────────────────────────────────────────────
    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val restaurant = repository.loginPartner(phone, password)
            if (restaurant != null) {
                _loggedInRestaurant.value = restaurant
                showMsg("Welcome back, ${restaurant.ownerName}! 👋")
            } else {
                showMsg("Invalid phone or password")
            }
            _isLoading.value = false
        }
    }

    // ─── Register ─────────────────────────────────────────
    fun registerRestaurant(
        ownerName: String, ownerPhone: String, ownerPassword: String,
        restaurantName: String, category: String,
        address: String, area: String,
        openingTime: String, closingTime: String,
        minOrder: Int, deliveryTime: String,
        fssaiNumber: String, gstNumber: String,
        emoji: String
    ) {
        if (ownerName.isBlank() || ownerPhone.isBlank() || restaurantName.isBlank()) {
            showMsg("Please fill all required fields"); return
        }
        if (ownerPhone.length != 10) {
            showMsg("Enter a valid 10-digit phone number"); return
        }

        viewModelScope.launch {
            _isLoading.value = true
            
            val newRestaurant = RegisteredRestaurant(
                id = UUID.randomUUID().toString().take(8),
                ownerName = ownerName.trim(),
                ownerPhone = ownerPhone.trim(),
                ownerPassword = ownerPassword,
                restaurantName = restaurantName.trim(),
                category = category,
                address = address.trim(),
                area = area.trim().ifBlank { "Jashpur Nagar" },
                openingTime = openingTime.ifBlank { "09:00 AM" },
                closingTime = closingTime.ifBlank { "10:00 PM" },
                minOrder = minOrder,
                deliveryTime = deliveryTime.ifBlank { "30-45 min" },
                fssaiNumber = fssaiNumber.trim(),
                gstNumber = gstNumber.trim(),
                emoji = emoji,
                status = RegistrationStatus.PENDING
            )

            val result = repository.registerRestaurant(newRestaurant)
            _isLoading.value = false

            if (result.isSuccess) {
                _loggedInRestaurant.value = newRestaurant
                showMsg("Registration submitted! Under review 🕐")
            } else {
                showMsg(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    // ─── Toggle open/closed ───────────────────────────────
    fun toggleOpenStatus() {
        val restaurant = _loggedInRestaurant.value ?: return
        viewModelScope.launch {
            repository.toggleOpenStatus(restaurant.id)
            refreshLocalState(restaurant.id)
            val status = _loggedInRestaurant.value?.isOpen ?: false
            showMsg(if (status) "Restaurant is now OPEN 🟢" else "Restaurant is now CLOSED 🔴")
        }
    }

    // ─── Add menu item ────────────────────────────────────
    fun addMenuItem(
        name: String, price: Int, description: String,
        category: String, isVeg: Boolean
    ) {
        val restaurant = _loggedInRestaurant.value ?: return
        if (name.isBlank()) { showMsg("Item name is required"); return }
        if (price <= 0) { showMsg("Enter a valid price"); return }

        viewModelScope.launch {
            val item = PartnerMenuItem(
                id = UUID.randomUUID().toString().take(8),
                name = name.trim(),
                price = price,
                description = description.trim(),
                category = category.ifBlank { "Other" },
                isVeg = isVeg
            )
            repository.addMenuItem(restaurant.id, item)
            refreshLocalState(restaurant.id)
            _showAddItemDialog.value = false
            showMsg("${item.name} added to menu ✅")
        }
    }

    // ─── Remove menu item ─────────────────────────────────
    fun removeMenuItem(itemId: String) {
        val restaurant = _loggedInRestaurant.value ?: return
        viewModelScope.launch {
            repository.removeMenuItem(restaurant.id, itemId)
            refreshLocalState(restaurant.id)
            showMsg("Item removed")
        }
    }

    // ─── Toggle item availability ─────────────────────────
    fun toggleItemAvailability(itemId: String) {
        val restaurant = _loggedInRestaurant.value ?: return
        viewModelScope.launch {
            repository.toggleItemAvailability(restaurant.id, itemId)
            refreshLocalState(restaurant.id)
        }
    }

    private suspend fun refreshLocalState(id: String) {
        _loggedInRestaurant.value = repository.getById(id)
    }

    fun setRegistrationStep(step: Int) { _registrationStep.value = step }
    fun showAddItemDialog() { _showAddItemDialog.value = true }
    fun hideAddItemDialog() { _showAddItemDialog.value = false }
    fun showMsg(msg: String) { _message.value = msg }
    fun clearMessage() { _message.value = null }

    fun logout() {
        _loggedInRestaurant.value = null
        _registrationStep.value = 1
    }
}
