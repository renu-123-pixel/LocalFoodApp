package com.data.repository

import com.data.model.User
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentFirebaseUser: FirebaseUser?
    
    suspend fun signUpWithEmail(email: String, password: String, fullName: String, phoneNumber: String, address: String): Result<Unit>
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    
    fun sendOtp(phoneNumber: String, onCodeSent: (String) -> Unit, onVerificationFailed: (Exception) -> Unit)
    suspend fun signInWithPhone(credential: AuthCredential): Result<Unit>
    
    suspend fun getUserDetails(uid: String): Result<User?>
    fun logout()
    
    fun isUserLoggedIn(): Boolean
}
