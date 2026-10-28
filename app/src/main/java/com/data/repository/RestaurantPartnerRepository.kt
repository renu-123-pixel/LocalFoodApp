package com.data.repository

import com.data.model.PartnerMenuItem
import com.data.model.RegisteredRestaurant
import com.data.model.RegistrationStatus
import com.data.model.Restaurant
import com.data.model.toRestaurant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantPartnerRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("restaurants")

    fun getRegisteredRestaurants(): Flow<List<RegisteredRestaurant>> = callbackFlow {
        val subscription = collection.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val list = snapshot?.documents?.mapNotNull { it.toObject(RegisteredRestaurant::class.java) } ?: emptyList()
            trySend(list)
        }
        awaitClose { subscription.remove() }
    }

    fun getApprovedRestaurants(): Flow<List<Restaurant>> = getRegisteredRestaurants().map { list ->
        list.filter { it.status == RegistrationStatus.APPROVED }.map { it.toRestaurant() }
    }

    suspend fun registerRestaurant(restaurant: RegisteredRestaurant): Result<Unit> = try {
        collection.document(restaurant.id).set(restaurant).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun loginPartner(phone: String, pass: String): RegisteredRestaurant? {
        val snapshot = collection
            .whereEqualTo("ownerPhone", phone)
            .whereEqualTo("ownerPassword", pass)
            .get().await()
        return snapshot.documents.firstOrNull()?.toObject(RegisteredRestaurant::class.java)
    }

    suspend fun getById(id: String): RegisteredRestaurant? {
        return collection.document(id).get().await().toObject(RegisteredRestaurant::class.java)
    }

    suspend fun toggleOpenStatus(id: String) {
        val r = getById(id) ?: return
        collection.document(id).update("isOpen", !r.isOpen).await()
    }

    suspend fun addMenuItem(restaurantId: String, item: PartnerMenuItem) {
        val r = getById(restaurantId) ?: return
        val newList = r.menuItems.toMutableList()
        newList.add(item)
        collection.document(restaurantId).update("menuItems", newList).await()
    }

    suspend fun removeMenuItem(restaurantId: String, itemId: String) {
        val r = getById(restaurantId) ?: return
        val newList = r.menuItems.filter { it.id != itemId }
        collection.document(restaurantId).update("menuItems", newList).await()
    }

    suspend fun toggleItemAvailability(restaurantId: String, itemId: String) {
        val r = getById(restaurantId) ?: return
        val newList = r.menuItems.map { 
            if (it.id == itemId) it.copy(isAvailable = !it.isAvailable) else it 
        }
        collection.document(restaurantId).update("menuItems", newList).await()
    }
}
