package com.data.repository

import com.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentFirebaseUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String,
        address: String
    ): Result<Unit> = try {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("User creation failed")
        
        val user = User(
            uid = uid,
            fullName = fullName,
            email = email,
            phoneNumber = phoneNumber,
            address = address,
            password = password, // Note: Storing password in Firestore is not recommended, but keeping for consistency with your model
            createdAt = Timestamp.now(),
            lastLogin = Timestamp.now()
        )
        
        firestore.collection("users").document(uid).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = try {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("Login failed")
        
        firestore.collection("users").document(uid)
            .update("lastLogin", Timestamp.now()).await()
            
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun sendOtp(
        phoneNumber: String,
        onCodeSent: (String) -> Unit,
        onVerificationFailed: (Exception) -> Unit
    ) {
        // Implementation handled in ViewModel usually due to Activity requirement
    }

    override suspend fun signInWithPhone(credential: AuthCredential): Result<Unit> = try {
        val authResult = auth.signInWithCredential(credential).await()
        val uid = authResult.user?.uid ?: throw Exception("Phone login failed")
        
        val doc = firestore.collection("users").document(uid).get().await()
        if (!doc.exists()) {
            val user = User(
                uid = uid,
                phoneNumber = authResult.user?.phoneNumber ?: "",
                createdAt = Timestamp.now(),
                lastLogin = Timestamp.now()
            )
            firestore.collection("users").document(uid).set(user).await()
        } else {
            firestore.collection("users").document(uid)
                .update("lastLogin", Timestamp.now()).await()
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getUserDetails(uid: String): Result<User?> = try {
        val doc = firestore.collection("users").document(uid).get().await()
        Result.success(doc.toObject(User::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun logout() {
        auth.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
