package com.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.data.model.LocationState
import com.data.model.Restaurant
import com.data.repository.AppRepository
import com.data.repository.LocationRepository
import com.data.repository.RestaurantPartnerRepository
import com.data.repository.RestaurantWithDistance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val locationState: LocationState = LocationState.Idle,
    val locationLabel: String = "Jashpur Nagar, CG 496331",
    val nearbyRestaurants: List<RestaurantWithDistance> = emptyList(),
    val allRestaurants: List<Restaurant> = emptyList(),
    val searchQuery: String = "",
    val vegOnly: Boolean = false,
    val snackbarMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val restaurantPartnerRepository: RestaurantPartnerRepository
) : AndroidViewModel(application) {

    private val locationRepo = LocationRepository(application)
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeRestaurants()
    }

    private fun observeRestaurants() {
        viewModelScope.launch {
            restaurantPartnerRepository.getApprovedRestaurants().collect { approved ->
                val all = AppRepository.restaurants + approved
                _uiState.update { it.copy(allRestaurants = all) }
                
                if (_uiState.value.locationState is LocationState.Found) {
                    refreshNearbyRestaurants()
                } else {
                    loadFallbackRestaurants()
                }
            }
        }
    }

    fun fetchLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(locationState = LocationState.Fetching) }

            if (!locationRepo.hasLocationPermission()) {
                _uiState.update { it.copy(locationState = LocationState.PermissionDenied) }
                loadFallbackRestaurants()
                return@launch
            }

            val userLocation = locationRepo.getUserLocation()
            val label = "${userLocation.cityName}, CG ${userLocation.pincode}"

            _uiState.update { it.copy(
                locationState = LocationState.Found(userLocation.latitude, userLocation.longitude, label),
                locationLabel = label
            ) }
            refreshNearbyRestaurants()
        }
    }

    private fun refreshNearbyRestaurants() {
        val state = _uiState.value.locationState
        if (state is LocationState.Found) {
            val userLoc = com.data.repository.UserLocation(state.lat, state.lng)
            val nearby = locationRepo.getNearbyRestaurants(userLoc, _uiState.value.allRestaurants)
            _uiState.update { it.copy(nearbyRestaurants = nearby) }
        }
    }

    private fun loadFallbackRestaurants() {
        val fallback = _uiState.value.allRestaurants.map { RestaurantWithDistance(it, 0.0, "") }
        _uiState.update { it.copy(nearbyRestaurants = fallback) }
    }

    fun updateSearch(query: String) { _uiState.update { it.copy(searchQuery = query) } }
    fun toggleVegFilter() { _uiState.update { it.copy(vegOnly = !it.vegOnly) } }
    fun showMessage(msg: String) { _uiState.update { it.copy(snackbarMessage = msg) } }
    fun clearMessage() { _uiState.update { it.copy(snackbarMessage = null) } }
    
    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) fetchLocation()
        else {
            _uiState.update { it.copy(locationState = LocationState.PermissionDenied) }
            loadFallbackRestaurants()
            showMessage("Showing all Jashpur restaurants")
        }
    }
}
