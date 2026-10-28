package com.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val password: String = "",
    val profileImage: String = "",
    val role: String = "customer",
    val createdAt: Timestamp? = null,
    val lastLogin: Timestamp? = null
) {
    // Helper properties for UI consistency
    val name: String get() = fullName
    val phone: String get() = phoneNumber
}
