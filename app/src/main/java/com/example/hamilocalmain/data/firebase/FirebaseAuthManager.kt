package com.example.hamilocalmain.data.firebase

import android.app.Activity
import android.content.Context
import com.example.hamilocalmain.data.model.User
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * Manager class for handling Firebase Authentication and User Profile operations.
 */
class FirebaseAuthManager(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Returns the currently logged-in Firebase user, or null if not logged in.
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Verifies phone number and sends OTP.
     */
    fun verifyPhoneNumber(phone: String, activity: Activity, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .requireSmsValidation(false)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Verifies the 6-digit OTP code. Returns Result with FirebaseUser on success.
     */
    suspend fun verifyOtp(code: String, verificationId: String): Result<FirebaseUser> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithCredential(credential)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs in with the given credential.
     */
    suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User is null after sign in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves user profile to Firestore after registration or update.
     */
    suspend fun saveUserProfile(user: User): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val userWithId = user.copy(id = uid)
            firestore.collection("users")
                .document(uid)
                .set(userWithId)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches user profile from Firestore. Returns null if user does not exist.
     */
    suspend fun getUserProfile(uid: String): User? {
        return try {
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Signs out the current user from Firebase Auth.
     */
    fun logout() {
        auth.signOut()
    }
}
