package com.data.repository

import com.data.model.DeliveryPartner
import com.data.model.FoodItem
import com.data.model.Restaurant

object AppRepository {

    val restaurants = listOf(
        Restaurant(
            id = 1,
            name = "Sharma Ji Ka Dhaba",
            category = "North Indian",
            rating = 4.5,
            deliveryTime = "20-30 min",
            minOrder = 80,
            emoji = "🍛",
            address = "Main Bazar, Jashpur Nagar",
            latitude = 22.8985,
            longitude = 84.1371,
            items = listOf(
                FoodItem(
                    101,
                    "Dal Tadka",
                    60,
                    "Homestyle yellow dal with ghee tadka",
                    true,
                    true,
                    1
                ),
                FoodItem(
                    102,
                    "Paneer Butter Masala",
                    120,
                    "Creamy tomato-based paneer curry",
                    true,
                    true,
                    1
                ),
                FoodItem(
                    103,
                    "Chicken Curry",
                    140,
                    "Desi style chicken in rich gravy",
                    false,
                    true,
                    1
                ),
                FoodItem(104, "Roti (2 pcs)", 20, "Fresh tandoor roti", true, false, 1),
                FoodItem(105, "Steamed Rice", 40, "Fluffy basmati rice", true, false, 1),
                FoodItem(106, "Aloo Sabzi", 50, "Spiced potato curry", true, false, 1)
            )
        ),
        Restaurant(
            id = 2,
            name = "Chhattisgarhi Zayka",
            category = "Local Special",
            rating = 4.8,
            deliveryTime = "15-25 min",
            minOrder = 60,
            emoji = "🌾",
            address = "Station Road, Jashpur",
            latitude = 22.8960,
            longitude = 84.1340,
            items = listOf(
                FoodItem(201, "Chila", 40, "Local rice flour pancake with chutney", true, true, 2),
                FoodItem(202, "Bafauri", 35, "Steamed chana dal dumplings", true, true, 2),
                FoodItem(203, "Iddhar", 45, "Traditional Chhattisgarhi rice cake", true, false, 2),
                FoodItem(204, "Muthiya", 50, "Spiced steamed snack", true, false, 2),
                FoodItem(205, "Faraa", 55, "Rice flour dumplings in curry", true, true, 2)
            )
        ),
        Restaurant(
            id = 3,
            name = "Jashpur Snacks Corner",
            category = "Snacks & Fast Food",
            rating = 4.3,
            deliveryTime = "10-20 min",
            minOrder = 50,
            emoji = "🍟",
            address = "Bus Stand, Jashpur Nagar",
            latitude = 22.8995,
            longitude = 84.1380,
            items = listOf(
                FoodItem(
                    301,
                    "Samosa (2 pcs)",
                    20,
                    "Crispy fried samosa with chutney",
                    true,
                    true,
                    3
                ),
                FoodItem(302, "Kachori", 15, "Spiced lentil stuffed fried bread", true, true, 3),
                FoodItem(303, "Poha", 30, "Flattened rice with onion and spices", true, true, 3),
                FoodItem(304, "Vada Pav", 25, "Spiced potato fritter in bun", true, false, 3),
                FoodItem(305, "Maggi", 35, "Classic masala noodles", true, false, 3),
                FoodItem(
                    306,
                    "Bread Omelette",
                    40,
                    "Egg omelette with toasted bread",
                    false,
                    true,
                    3
                )
            )
        ),
        Restaurant(
            id = 4,
            name = "Sweet House Jashpur",
            category = "Sweets & Drinks",
            rating = 4.6,
            deliveryTime = "15-20 min",
            minOrder = 40,
            emoji = "🍮",
            address = "Gandhi Chowk, Jashpur",
            latitude = 22.8970,
            longitude = 84.1355,
            items = listOf(
                FoodItem(401, "Jalebi (250g)", 50, "Crispy syrup-soaked jalebi", true, true, 4),
                FoodItem(
                    402,
                    "Gulab Jamun (4 pcs)",
                    40,
                    "Soft milk-solid dumplings in syrup",
                    true,
                    true,
                    4
                ),
                FoodItem(403, "Lassi", 30, "Chilled sweet yogurt drink", true, true, 4),
                FoodItem(404, "Masala Chai", 15, "Spiced hot milk tea", true, false, 4),
                FoodItem(405, "Cold Coffee", 45, "Chilled blended coffee with milk", true, false, 4)
            )
        )
    )

    val deliveryPartners = listOf(
        DeliveryPartner(1, "Ramesh Yadav", "9876543210", 4.7, "CG10 AB 1234"),
        DeliveryPartner(2, "Suresh Patel", "9876543211", 4.9, "CG10 CD 5678")
    )
}
