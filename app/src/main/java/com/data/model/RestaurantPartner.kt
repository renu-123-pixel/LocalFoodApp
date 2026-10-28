package com.data.model

// Registration status of a restaurant
enum class RegistrationStatus(val label: String, val emoji: String) {
    PENDING("Under Review", "🕐"),
    APPROVED("Live on App", "✅"),
    REJECTED("Not Approved", "❌")
}

// A restaurant that registered itself (partner side)
data class RegisteredRestaurant(
    val id: String,
    val ownerName: String,
    val ownerPhone: String,
    val ownerPassword: String,
    val restaurantName: String,
    val category: String,
    val address: String,
    val area: String,
    val openingTime: String,
    val closingTime: String,
    val minOrder: Int,
    val deliveryTime: String,
    val fssaiNumber: String,
    val gstNumber: String,
    val emoji: String,
    val latitude: Double = 22.8974,
    val longitude: Double = 84.1358,
    var status: RegistrationStatus = RegistrationStatus.PENDING,
    var isOpen: Boolean = true,
    val menuItems: MutableList<PartnerMenuItem> = mutableListOf(),
    val rating: Double = 0.0,
    val totalOrders: Int = 0
)

data class PartnerMenuItem(
    val id: String,
    val name: String,
    val price: Int,
    val description: String,
    val category: String,
    val isVeg: Boolean,
    var isAvailable: Boolean = true
)

// What the customer side sees (converted from RegisteredRestaurant)
fun RegisteredRestaurant.toRestaurant(): Restaurant {
    return Restaurant(
        id = this.id.hashCode(),
        name = this.restaurantName,
        category = this.category,
        rating = if (this.rating == 0.0) 4.0 else this.rating,
        deliveryTime = this.deliveryTime,
        minOrder = this.minOrder,
        emoji = this.emoji,
        address = "${this.address}, ${this.area}",
        latitude = this.latitude,
        longitude = this.longitude,
        items = this.menuItems
            .filter { it.isAvailable }
            .map { menuItem ->
                FoodItem(
                    id = menuItem.id.hashCode(),
                    name = menuItem.name,
                    price = menuItem.price,
                    description = menuItem.description,
                    isVeg = menuItem.isVeg,
                    isPopular = false,
                    restaurantId = this.id.hashCode()
                )
            }
    )
}
