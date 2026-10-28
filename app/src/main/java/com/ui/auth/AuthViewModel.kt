package com.ui.auth

import android.app.Activity
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.repository.AuthRepository
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _signUpState = MutableStateFlow<AuthState<Unit>>(AuthState.Idle)
    val signUpState: StateFlow<AuthState<Unit>> = _signUpState.asStateFlow()

    private val _signInState = MutableStateFlow<AuthState<Unit>>(AuthState.Idle)
    val signInState: StateFlow<AuthState<Unit>> = _signInState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<AuthState<Unit>>(AuthState.Idle)
    val resetPasswordState: StateFlow<AuthState<Unit>> = _resetPasswordState.asStateFlow()

    private val _otpState = MutableStateFlow<AuthState<String>>(AuthState.Idle)
    val otpState: StateFlow<AuthState<String>> = _otpState.asStateFlow()

    fun signUp(email: String, password: String, fullName: String, phoneNumber: String, address: String) {
        if (!validateSignUp(email, password, fullName, phoneNumber, address)) return

        viewModelScope.launch {
            _signUpState.value = AuthState.Loading
            val result = repository.signUpWithEmail(email, password, fullName, phoneNumber, address)
            _signUpState.value = if (result.isSuccess) {
                AuthState.Success(Unit)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Sign up failed")
            }
        }
    }

    private fun validateSignUp(email: String, password: String, fullName: String, phoneNumber: String, address: String): Boolean {
        return when {
            fullName.isBlank() -> {
                _signUpState.value = AuthState.Error("Full name cannot be empty")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _signUpState.value = AuthState.Error("Invalid email address")
                false
            }
            password.length < 6 -> {
                _signUpState.value = AuthState.Error("Password must be at least 6 characters")
                false
            }
            phoneNumber.length < 10 -> {
                _signUpState.value = AuthState.Error("Invalid phone number")
                false
            }
            address.isBlank() -> {
                _signUpState.value = AuthState.Error("Address cannot be empty")
                false
            }
            else -> true
        }
    }

    fun signIn(email: String, password: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _signInState.value = AuthState.Error("Invalid email address")
            return
        }
        if (password.isEmpty()) {
            _signInState.value = AuthState.Error("Password cannot be empty")
            return
        }

        viewModelScope.launch {
            _signInState.value = AuthState.Loading
            val result = repository.signInWithEmail(email, password)
            _signInState.value = if (result.isSuccess) {
                AuthState.Success(Unit)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun resetPassword(email: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _resetPasswordState.value = AuthState.Error("Invalid email address")
            return
        }

        viewModelScope.launch {
            _resetPasswordState.value = AuthState.Loading
            val result = repository.sendPasswordResetEmail(email)
            _resetPasswordState.value = if (result.isSuccess) {
                AuthState.Success(Unit)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to send reset email")
            }
        }
    }

    fun sendOtp(phoneNumber: String, activity: Activity) {
        if (phoneNumber.length < 10) {
            _otpState.value = AuthState.Error("Invalid phone number")
            return
        }
        
        _otpState.value = AuthState.Loading
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhone(credential)
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                _otpState.value = AuthState.Error(e.message ?: "Verification failed")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _otpState.value = AuthState.Success(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(com.google.firebase.auth.FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(verificationId: String, otp: String) {
        if (otp.length < 6) {
            _otpState.value = AuthState.Error("Invalid OTP")
            return
        }
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithPhone(credential)
    }

    private fun signInWithPhone(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _signInState.value = AuthState.Loading
            val result = repository.signInWithPhone(credential)
            _signInState.value = if (result.isSuccess) {
                AuthState.Success(Unit)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Phone login failed")
            }
        }
    }

    fun logout() {
        repository.logout()
        _signInState.value = AuthState.Idle
    }

    fun clearStates() {
        _signUpState.value = AuthState.Idle
        _signInState.value = AuthState.Idle
        _resetPasswordState.value = AuthState.Idle
        _otpState.value = AuthState.Idle
    }
}
