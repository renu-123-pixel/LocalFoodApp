package com.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.data.model.CartItem
import com.data.model.FoodItem

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey val foodId: Int,
    val name: String,
    val price: Int,
    val description: String,
    val isVeg: Boolean,
    val restaurantId: Int,
    val restaurantName: String,
    val imageUrl: String?,
    val quantity: Int
)

fun CartEntity.toCartItem() = CartItem(
    foodItem = FoodItem(
        id = foodId,
        name = name,
        price = price,
        description = description,
        isVeg = isVeg,
        isPopular = false,
        restaurantId = restaurantId,
        imageUrl = imageUrl
    ),
    restaurantId = restaurantId,
    restaurantName = restaurantName,
    quantity = quantity
)

fun CartItem.toEntity() = CartEntity(
    foodId = foodItem.id,
    name = foodItem.name,
    price = foodItem.price,
    description = foodItem.description,
    isVeg = foodItem.isVeg,
    restaurantId = restaurantId,
    restaurantName = restaurantName,
    imageUrl = foodItem.imageUrl,
    quantity = quantity
)
