package com.example.flowride.data

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ActiveRental(
    val id: String = "",
    val vehicleType: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val pickupLocation: String = "",
    val duration: String = "",
    val price: Int = 0,
    val paymentMethod: String = "",
    val status: String = "active",
    val userName: String = "Nepoznato",
    val userEmail: String = "",
    val uid: String = ""
)

object RentalRepository {
    private val _rentals = mutableStateListOf<ActiveRental>()
    private val db = FirebaseFirestore.getInstance()

    val rentals: List<ActiveRental> get() {
        val email = UserRepository.currentUser?.email
        return if (email != null) _rentals.filter { it.userEmail == email }
        else emptyList()
    }

    val allRentalsForAdmin: List<ActiveRental> get() = _rentals

    suspend fun loadRentals() {
        try {
            val snapshot = db.collectionGroup("rentals").get().await()
            _rentals.clear()
            _rentals.addAll(snapshot.toObjects(ActiveRental::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addRental(rental: ActiveRental) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        try {
            db.collection("users").document(uid)
                .collection("rentals").document(rental.id)
                .set(rental).await()
            _rentals.add(rental)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteRentalsForUser(email: String) {
        try {
            val snapshot = db.collectionGroup("rentals")
                .whereEqualTo("userEmail", email).get().await()
            snapshot.documents.forEach { it.reference.delete().await() }
            _rentals.removeIf { it.userEmail == email }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun confirmRental(rentalId: String) {
        try {
            val snapshot = db.collectionGroup("rentals")
                .whereEqualTo("id", rentalId).get().await()
            snapshot.documents.forEach { doc ->
                doc.reference.update("status", "completed").await()
            }
            val index = _rentals.indexOfFirst { it.id == rentalId }
            if (index != -1) {
                _rentals[index] = _rentals[index].copy(status = "completed")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun findRentalById(id: String): ActiveRental? {
        return try {
            val snapshot = db.collectionGroup("rentals")
                .whereEqualTo("id", id).get().await()
            snapshot.documents.firstOrNull()?.toObject(ActiveRental::class.java)
        } catch (e: Exception) {
            null
        }
    }
}