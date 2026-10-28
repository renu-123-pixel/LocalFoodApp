package com.data.model

data class FoodItem(
    val id: Int,
    val name: String,
    val price: Int,
    val description: String,
    val isVeg: Boolean,
    val isPopular: Boolean,
    val restaurantId: Int,
    val imageUrl: String? = null
)

data class Restaurant(
    val id: Int,
    val name: String,
    val category: String,
    val rating: Double,
    val deliveryTime: String,
    val minOrder: Int,
    val emoji: String,
    val address: String,
    val items: List<FoodItem>,
    val latitude: Double = 22.8974,
    val longitude: Double = 84.1358,
    val imageUrl: String? = null
)

data class CartItem(
    val foodItem: FoodItem,
    val restaurantId: Int,
    val restaurantName: String,
    var quantity: Int = 1
)

data class DeliveryPartner(
    val id: Int,
    val name: String,
    val phone: String,
    val rating: Double,
    val vehicleNumber: String
)

data class Order(
    val id: String,
    val items: List<CartItem>,
    val restaurantName: String,
    val totalAmount: Int,
    val deliveryAddress: String,
    val deliveryPartner: DeliveryPartner,
    val time: String,
    val date: String,
    var status: OrderStatus = OrderStatus.PLACED
)

enum class OrderStatus(val label: String, val emoji: String) {
    PLACED("Order Placed", "📋"),
    ACCEPTED("Restaurant Accepted", "✅"),
    PREPARING("Being Prepared", "👨‍🍳"),
    ASSIGNED("Rider Assigned", "🛵"),
    OUT_FOR_DELIVERY("Out for Delivery", "🚀"),
    DELIVERED("Delivered", "🎉")
}

sealed class LocationState {
    object Idle : LocationState()
    object Fetching : LocationState()
    data class Found(val lat: Double, val lng: Double, val label: String) : LocationState()
    object PermissionDenied : LocationState()
    object Failed : LocationState()
}
