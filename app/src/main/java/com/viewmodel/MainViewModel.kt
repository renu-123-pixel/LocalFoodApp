package com.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.data.local.CartDao
import com.data.local.toCartItem
import com.data.local.toEntity
import com.data.model.CartItem
import com.data.model.FoodItem
import com.data.model.LocationState
import com.data.model.Order
import com.data.model.OrderStatus
import com.data.model.RegistrationStatus
import com.data.model.Restaurant
import com.data.model.User
import com.data.model.toRestaurant
import com.data.repository.AppRepository
import com.data.repository.AuthRepository
import com.data.repository.LocationRepository
import com.data.repository.RestaurantPartnerRepository
import com.data.repository.RestaurantWithDistance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val authRepository: AuthRepository,
    private val cartDao: CartDao,
    private val restaurantPartnerRepository: RestaurantPartnerRepository
) : AndroidViewModel(application) {

    private val locationRepo = LocationRepository(application)

    // ─── Auth ─────────────────────────────────────────────
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkUserSession()
        observeRestaurants()
    }

    private fun checkUserSession() {
        val firebaseUser = authRepository.currentFirebaseUser
        if (firebaseUser != null) {
            viewModelScope.launch {
                val result = authRepository.getUserDetails(firebaseUser.uid)
                _currentUser.value = result.getOrNull()
            }
        }
    }

    // ─── Restaurants (Source: Firestore Database) ──────────
    private val _allAvailableRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val allAvailableRestaurants: StateFlow<List<Restaurant>> = _allAvailableRestaurants.asStateFlow()

    private fun observeRestaurants() {
        viewModelScope.launch {
            restaurantPartnerRepository.getRegisteredRestaurants().collect { registered ->
                val approved = registered
                    .filter { it.status == RegistrationStatus.APPROVED }
                    .map { it.toRestaurant() }
                
                _allAvailableRestaurants.value = approved
                
                val state = _locationState.value
                if (state is LocationState.Found) {
                    refreshNearbyRestaurants(state.lat, state.lng)
                }
            }
        }
    }

    // ─── Location ─────────────────────────────────────────
    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _locationLabel = MutableStateFlow("Jashpur Nagar, CG 496331")
    val locationLabel: StateFlow<String> = _locationLabel.asStateFlow()

    private val _nearbyRestaurants = MutableStateFlow<List<RestaurantWithDistance>>(emptyList())
    val nearbyRestaurants: StateFlow<List<RestaurantWithDistance>> = _nearbyRestaurants.asStateFlow()

    fun fetchLocationAndLoadRestaurants() {
        viewModelScope.launch {
            _locationState.value = LocationState.Fetching

            if (!locationRepo.hasLocationPermission()) {
                _locationState.value = LocationState.PermissionDenied
                loadFallbackRestaurants()
                return@launch
            }

            val userLocation = locationRepo.getUserLocation()

            _locationState.value = LocationState.Found(
                lat = userLocation.latitude,
                lng = userLocation.longitude,
                label = "${userLocation.cityName}, CG ${userLocation.pincode}"
            )

            _locationLabel.value = "${userLocation.cityName}, CG ${userLocation.pincode}"
            refreshNearbyRestaurants(userLocation.latitude, userLocation.longitude)
        }
    }

    private fun refreshNearbyRestaurants(lat: Double, lng: Double) {
        val userLocation = com.data.repository.UserLocation(lat, lng)
        val nearby = locationRepo.getNearbyRestaurants(userLocation, _allAvailableRestaurants.value, radiusKm = 10.0)
        _nearbyRestaurants.value = nearby
    }

    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) fetchLocationAndLoadRestaurants()
        else {
            _locationState.value = LocationState.PermissionDenied
            loadFallbackRestaurants()
            showMessage("Showing all Jashpur restaurants")
        }
    }

    private fun loadFallbackRestaurants() {
        _nearbyRestaurants.value = _allAvailableRestaurants.value.map { r ->
            RestaurantWithDistance(r, 0.0, "")
        }
    }

    // ─── Cart (Persistent via Room) ───────────────────────
    val cartItems: StateFlow<List<CartItem>> = cartDao.getCartItems()
        .map { entities -> entities.map { it.toCartItem() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addToCart(item: FoodItem, restaurant: Restaurant): Boolean {
        val current = cartItems.value
        if (current.isNotEmpty() && current.first().restaurantId != restaurant.id) return false
        
        viewModelScope.launch {
            val existing = cartDao.getCartItemById(item.id)
            if (existing != null) {
                cartDao.updateCartItem(existing.copy(quantity = existing.quantity + 1))
            } else {
                cartDao.addToCart(CartItem(item, restaurant.id, restaurant.name).toEntity())
            }
        }
        showMessage("${item.name} added 🛒")
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

    fun getItemQty(itemId: Int): Int {
        return cartItems.value.find { it.foodItem.id == itemId }?.quantity ?: 0
    }

    fun clearCartAndAdd(item: FoodItem, restaurant: Restaurant) {
        viewModelScope.launch {
            cartDao.clearCart()
            cartDao.addToCart(CartItem(item, restaurant.id, restaurant.name).toEntity())
        }
        showMessage("${item.name} added 🛒")
    }

    val cartTotal: Int get() = cartItems.value.sumOf { it.foodItem.price * it.quantity }
    val cartCount: Int get() = cartItems.value.sumOf { it.quantity }
    val deliveryFee = 20
    val platformFee = 5
    val grandTotal: Int get() = cartTotal + deliveryFee + platformFee

    // ─── Orders ───────────────────────────────────────────
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder.asStateFlow()

    private val _trackingStep = MutableStateFlow(0)
    val trackingStep: StateFlow<Int> = _trackingStep.asStateFlow()

    fun placeOrder(): Order? {
        val cart = cartItems.value
        if (cart.isEmpty()) return null
        val partner = AppRepository.deliveryPartners.random()
        val now = Calendar.getInstance()
        val order = Order(
            id = "JSP${System.currentTimeMillis().toString().takeLast(6)}",
            items = cart.toList(),
            restaurantName = cart.first().restaurantName,
            totalAmount = grandTotal,
            deliveryAddress = _currentUser.value?.address ?: "Jashpur Nagar, CG 496331",
            deliveryPartner = partner,
            time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now.time),
            date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(now.time)
        )
        _currentOrder.value = order
        _orders.value = listOf(order) + _orders.value
        
        viewModelScope.launch {
            cartDao.clearCart()
        }
        
        _trackingStep.value = 0
        startTracking()
        return order
    }

    private fun startTracking() {
        viewModelScope.launch {
            val totalSteps = OrderStatus.values().size
            while (_trackingStep.value < totalSteps - 1) {
                delay(3000)
                _trackingStep.value++
            }
        }
    }

    // ─── Utilities ────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _vegOnly = MutableStateFlow(false)
    val vegOnly: StateFlow<Boolean> = _vegOnly.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun updateSearch(query: String) { _searchQuery.value = query }
    fun toggleVegFilter() { _vegOnly.value = !_vegOnly.value }
    fun showMessage(msg: String) { _snackbarMessage.value = msg }
    fun clearMessage() { _snackbarMessage.value = null }
    
    fun register(name: String, email: String, phone: String, password: String, address: String) {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            showMessage("Please fill all required fields"); return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signUpWithEmail(email, password, name, phone, address)
            if (result.isSuccess) {
                val firebaseUser = authRepository.currentFirebaseUser
                if (firebaseUser != null) {
                    val userDetails = authRepository.getUserDetails(firebaseUser.uid)
                    _currentUser.value = userDetails.getOrNull()
                }
                showMessage("Account created! Welcome, $name 🎉")
            } else {
                showMessage(result.exceptionOrNull()?.message ?: "Registration failed")
            }
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            showMessage("Please enter email and password"); return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signInWithEmail(email, password)
            if (result.isSuccess) {
                val firebaseUser = authRepository.currentFirebaseUser
                if (firebaseUser != null) {
                    val userDetails = authRepository.getUserDetails(firebaseUser.uid)
                    _currentUser.value = userDetails.getOrNull()
                    if (_currentUser.value != null) {
                        showMessage("Welcome back! 👋")
                    } else {
                        showMessage("Failed to load user profile")
                    }
                }
            } else {
                showMessage(result.exceptionOrNull()?.message ?: "Login failed")
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        authRepository.logout()
        _currentUser.value = null
        viewModelScope.launch {
            cartDao.clearCart()
        }
        _locationState.value = LocationState.Idle
        showMessage("Logged out successfully")
    }
}
