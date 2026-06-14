package com.example.flowride.data

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val profilePicture: String? = null,
    val isAdmin: Boolean = false
)

object UserRepository {
    private var _currentUser by mutableStateOf<UserProfile?>(null)
    val currentUser: UserProfile? get() = _currentUser

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun init() {
        val firebaseUser = auth.currentUser ?: return
        try {
            val doc = db.collection("users").document(firebaseUser.uid).get().await()
            _currentUser = doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return false
            val doc = db.collection("users").document(uid).get().await()
            _currentUser = doc.toObject(UserProfile::class.java)
            saveFcmToken(uid)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        address: String,
        password: String
    ): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return false
            val isAdmin = email.lowercase().contains("admin")
            val user = UserProfile(
                uid = uid,
                name = name,
                email = email,
                phone = phone,
                address = address,
                isAdmin = isAdmin
            )
            db.collection("users").document(uid).set(user).await()
            _currentUser = user
            saveFcmToken(uid)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun logout() {
        auth.signOut()
        _currentUser = null
    }

    suspend fun deleteAccount() {
        val uid = auth.currentUser?.uid ?: return
        val email = _currentUser?.email ?: return
        try {
            db.collection("users").document(uid).delete().await()
            RentalRepository.deleteRentalsForUser(email)
            auth.currentUser?.delete()?.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _currentUser = null
    }

    suspend fun updateProfile(name: String, phone: String, address: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val updates = mapOf(
                "name" to name,
                "phone" to phone,
                "address" to address
            )
            db.collection("users").document(uid).update(updates).await()
            _currentUser = _currentUser?.copy(name = name, phone = phone, address = address)
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun saveFcmToken(uid: String) {
        try {
            val token = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
            db.collection("users").document(uid).update("fcmToken", token).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}