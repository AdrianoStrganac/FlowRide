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
    val status: String = "active", // active, in_progress, completed
    val userName: String = "Nepoznato",
    val userEmail: String = "",
    val uid: String = "",
    val startTimeMillis: Long? = null,
    val durationMinutes: Int = 60
)

object RentalRepository {
    private val _rentals = mutableStateListOf<ActiveRental>()
    private val db = FirebaseFirestore.getInstance()

    val rentals: List<ActiveRental> get() = _rentals

    val allRentalsForAdmin: List<ActiveRental> get() = _rentals

    suspend fun loadRentals() {
        try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                ?: return

            // 1. Prvo učitaj rezervacije za trenutnog korisnika
            val snapshot = db.collection("users")
                .document(uid)
                .collection("rentals")
                .get().await()

            val userRentals = snapshot.toObjects(ActiveRental::class.java)

            _rentals.clear()
            _rentals.addAll(userRentals)

            android.util.Log.d("RentalRepo", "Loaded ${userRentals.size} rentals for uid=$uid")

            // 2. Ako je korisnik admin, učitaj SVE rezervacije od SVIH korisnika
            val isAdmin = UserRepository.currentUser?.isAdmin ?: false
            if (isAdmin) {
                val allUsers = db.collection("users").get().await()
                val allRentalsList = mutableListOf<ActiveRental>()

                for (userDoc in allUsers.documents) {
                    val rentalsSnap = userDoc.reference.collection("rentals").get().await()
                    allRentalsList.addAll(rentalsSnap.toObjects(ActiveRental::class.java))
                }

                _rentals.clear()
                _rentals.addAll(allRentalsList)
                android.util.Log.d(
                    "RentalRepo",
                    "Admin mode: Loaded ${_rentals.size} total rentals"
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("RentalRepo", "Error loading rentals: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun addRental(rental: ActiveRental) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        try {
            val rentalWithUid = rental.copy(
                uid = uid,
                userEmail = UserRepository.currentUser?.email ?: rental.userEmail
            )
            db.collection("users").document(uid)
                .collection("rentals").document(rental.id)
                .set(rentalWithUid).await()
            _rentals.add(rentalWithUid)
            android.util.Log.d("RentalRepo", "Added rental ${rental.id}")
        } catch (e: Exception) {
            android.util.Log.e("RentalRepo", "Error adding: ${e.message}", e)
        }
    }

    suspend fun deleteRentalsForUser(email: String) {
        try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
            val snapshot = db.collection("users").document(uid)
                .collection("rentals").get().await()
            snapshot.documents.forEach { it.reference.delete().await() }
            _rentals.removeIf { it.userEmail == email }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun startRental(rentalId: String) {
        try {
            val allUsers = db.collection("users").get().await()
            val startTime = System.currentTimeMillis()

            for (userDoc in allUsers.documents) {
                val rentalRef = userDoc.reference.collection("rentals").document(rentalId)
                val snap = rentalRef.get().await()
                if (snap.exists()) {
                    val updates = mapOf(
                        "status" to "in_progress",
                        "startTimeMillis" to startTime
                    )
                    rentalRef.update(updates).await()

                    // Ažuriraj lokalnu listu
                    val index = _rentals.indexOfFirst { it.id == rentalId }
                    if (index != -1) {
                        _rentals[index] = _rentals[index].copy(
                            status = "in_progress",
                            startTimeMillis = startTime
                        )
                    }
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun confirmRental(rentalId: String) {
        try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            // Pokušaj naći u svim korisnicima
            val allUsers = db.collection("users").get().await()
            allUsers.documents.forEach { userDoc ->
                val rentalRef = userDoc.reference.collection("rentals").document(rentalId)
                val snap = rentalRef.get().await()
                if (snap.exists()) {
                    rentalRef.update("status", "completed").await()
                }
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
            android.util.Log.d("RentalRepo", "findRentalById called with id='$id'")
            val allUsers = db.collection("users").get().await()
            android.util.Log.d("RentalRepo", "Found ${allUsers.documents.size} users")

            for (userDoc in allUsers.documents) {
                android.util.Log.d("RentalRepo", "Checking user: ${userDoc.id}")
                val snap = userDoc.reference.collection("rentals").document(id).get().await()
                android.util.Log.d("RentalRepo", "  rental doc exists: ${snap.exists()}")
                if (snap.exists()) {
                    return snap.toObject(ActiveRental::class.java)
                }
            }
            android.util.Log.d("RentalRepo", "Rental not found in any user")
            null
        } catch (e: Exception) {
            android.util.Log.e("RentalRepo", "Error: ${e.message}", e)
            null
        }
    }
}