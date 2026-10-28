package com.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.data.model.Restaurant
import com.ui.theme.JashpurBhojanTheme
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.roundToInt

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String = "Jashpur Nagar",
    val pincode: String = "496331"
)

data class RestaurantWithDistance(
    val restaurant: Restaurant,
    val distanceKm: Double,
    val distanceLabel: String
)

class LocationRepository(private val context: Context) {

    private val JASHPUR_LAT = 22.8974
    private val JASHPUR_LNG = 84.1358

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): UserLocation? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            continuation.resume(
                                UserLocation(
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                            )
                        } else {
                            continuation.resume(null)
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            } catch (e: SecurityException) {
                continuation.resume(null)
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getFreshLocation(): UserLocation? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { continuation ->
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000L
            ).setMaxUpdates(1).build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation
                    fusedLocationClient.removeLocationUpdates(this)
                    if (location != null) {
                        continuation.resume(
                            UserLocation(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    } else {
                        continuation.resume(null)
                    }
                }
            }

            try {
                fusedLocationClient.requestLocationUpdates(
                    request, callback, Looper.getMainLooper()
                )
                continuation.invokeOnCancellation {
                    fusedLocationClient.removeLocationUpdates(callback)
                }
            } catch (e: SecurityException) {
                continuation.resume(null)
            }
        }
    }

    suspend fun getUserLocation(): UserLocation {
        val cached = getLastLocation()
        if (cached != null) return cached

        val fresh = getFreshLocation()
        if (fresh != null) return fresh

        return UserLocation(JASHPUR_LAT, JASHPUR_LNG, "Jashpur Nagar", "496331")
    }

    fun calculateDistanceKm(
        userLat: Double, userLng: Double,
        restaurantLat: Double, restaurantLng: Double
    ): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            userLat, userLng,
            restaurantLat, restaurantLng,
            results
        )
        return (results[0] / 1000.0 * 10).roundToInt() / 10.0
    }

    fun getNearbyRestaurants(
        userLocation: UserLocation,
        allRestaurants: List<Restaurant>,
        radiusKm: Double = 10.0
    ): List<RestaurantWithDistance> {
        return allRestaurants
            .map { restaurant ->
                val dist = calculateDistanceKm(
                    userLocation.latitude, userLocation.longitude,
                    restaurant.latitude, restaurant.longitude
                )
                RestaurantWithDistance(
                    restaurant = restaurant,
                    distanceKm = dist,
                    distanceLabel = if (dist < 1.0) "${(dist * 1000).roundToInt()} m away"
                                    else "$dist km away"
                )
            }
            .filter { it.distanceKm <= radiusKm }
            .sortedBy { it.distanceKm }
    }
}

// ─── Debug Preview for Location Data ──────────────────────

@Composable
fun LocationRepositoryDebugView(items: List<RestaurantWithDistance>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("Location Logic Preview", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1A237E))
        Text("Visualizing restaurant distances from user", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(12.dp))
        
        items.forEach { item ->
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.restaurant.name, fontWeight = FontWeight.Medium)
                    Text(item.distanceLabel, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                }
                Text("${item.restaurant.address}", fontSize = 11.sp, color = Color.Gray)
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationRepositoryPreview() {
    JashpurBhojanTheme {
        val mockData = listOf(
            RestaurantWithDistance(
                Restaurant(1, "Sharma Ji Ka Dhaba", "North Indian", 4.5, "20-30 min", 80, "🍛", "Main Bazar", emptyList()),
                0.8,
                "800 m away"
            ),
            RestaurantWithDistance(
                Restaurant(2, "Chhattisgarhi Zayka", "Local Special", 4.8, "15-25 min", 60, "🌾", "Station Road", emptyList()),
                2.5,
                "2.5 km away"
            ),
            RestaurantWithDistance(
                Restaurant(3, "Jashpur Snacks Corner", "Snacks", 4.3, "10-20 min", 50, "🍟", "Bus Stand", emptyList()),
                5.2,
                "5.2 km away"
            )
        )
        LocationRepositoryDebugView(mockData)
    }
}
