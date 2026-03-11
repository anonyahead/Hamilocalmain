package com.example.hamilocalmain.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hamilocalmain.data.firebase.FirebaseAuthManager
import com.example.hamilocalmain.data.model.Address
import com.example.hamilocalmain.data.model.User
import com.example.hamilocalmain.data.model.UserType
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Authentication state representing different phases of the login/registration process.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Manages authentication state. Coordinates FirebaseAuthManager with UI.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = FirebaseAuthManager(application)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // State fields used by the OTP verification process
    private val isLoading = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val verificationId = MutableStateFlow<String?>(null)

    init {
        // Initial check for current user could go here
    }

    /**
     * Sends OTP to the provided phone number.
     * 
     * @param phoneNumber The phone number to send the verification code to.
     * @param activity The activity context required for Firebase phone auth.
     */
    fun sendOtp(phoneNumber: String, activity: Activity) {
        isLoading.value = true
        
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification completed
                isLoading.value = false
                _authState.value = AuthState.Success
            }
            
            override fun onVerificationFailed(e: FirebaseException) {
                // Verification failed
                isLoading.value = false
                errorMessage.value = e.message ?: "Verification failed"
                _authState.value = AuthState.Error(errorMessage.value ?: "Verification failed")
            }
            
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // Code sent to phone
                this@AuthViewModel.verificationId.value = verificationId
                isLoading.value = false
                // State remains success or transition to a 'CodeSent' state if defined
            }
        }
        
        authManager.verifyPhoneNumber(phoneNumber, activity, callbacks)
    }

    /**
     * Verifies the OTP code received by the user.
     * 
     * @param code The 6-digit verification code.
     * @param verificationId The ID received when the OTP was sent.
     */
    fun verifyOtp(code: String, verificationId: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authManager.verifyOtp(code, verificationId)
            result.onSuccess {
                _authState.value = AuthState.Success
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Verification failed")
            }
        }
    }

    /**
     * Saves or updates the user profile details.
     * 
     * @param name Full name of the user.
     * @param userType Type of user (FARMER or CONSUMER).
     * @param address Physical address details.
     */
    fun saveProfile(name: String, userType: UserType, address: Address) {
        viewModelScope.launch {
            val currentId = authManager.getCurrentUser()?.uid ?: ""
            val user = User(
                id = currentId,
                name = name,
                userType = userType,
                address = address
            )
            _authState.value = AuthState.Loading
            val result = authManager.saveUserProfile(user)
            result.onSuccess {
                _currentUser.value = user
                _authState.value = AuthState.Success
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Failed to save profile")
            }
        }
    }

    /**
     * Signs out the current user and clears local state.
     */
    fun logout() {
        authManager.logout()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }
}
