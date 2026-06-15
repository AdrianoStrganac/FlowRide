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
    private val _adminScannedRentals = mutableStateListOf<ActiveRental>()

    private val db = FirebaseFirestore.getInstance()

    val rentals: List<ActiveRental> get() = _rentals

    val allRentalsForAdmin: List<ActiveRental> get() = _adminScannedRentals

    fun clearRentals() {
        _rentals.clear()
        // NE brišemo _adminScannedRentals ovdje
    }

    fun clearAll() {
        _rentals.clear()
        _adminScannedRentals.clear()
    }

    fun updateScannedRental(rental: ActiveRental) {
        val index = _adminScannedRentals.indexOfFirst { it.id == rental.id }
        if (index != -1) {
            _adminScannedRentals[index] = rental
        }
    }

    suspend fun loadRentals() {
        try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            android.util.Log.d("RentalRepo", "loadRentals start: uid=$uid")

            if (uid == null) {
                _rentals.clear()
                android.util.Log.d("RentalRepo", "No user logged in, clearing rentals")
                return
            }

            _rentals.clear()

            val isAdmin = UserRepository.currentUser?.isAdmin ?: false
            android.util.Log.d("RentalRepo", "isAdmin=$isAdmin, currentUser=${UserRepository.currentUser?.email}")

            if (!isAdmin) {
                val snapshot = db.collection("users")
                    .document(uid)
                    .collection("rentals")
                    .get().await()
                val userRentals = snapshot.toObjects(ActiveRental::class.java)
                android.util.Log.d("RentalRepo", "Loaded ${userRentals.size} rentals from Firestore")
                userRentals.forEach {
                    android.util.Log.d("RentalRepo", "  rental: id=${it.id}, status=${it.status}")
                }
                _rentals.addAll(userRentals)
            }
        } catch (e: Exception) {
            android.util.Log.e("RentalRepo", "Error: ${e.message}", e)
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

    // Spremi skenirani rental ID u Firestore
    suspend fun saveScannedRentalId(rentalId: String) {
        val adminUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        try {
            db.collection("users").document(adminUid)
                .collection("scannedRentals").document(rentalId)
                .set(mapOf("rentalId" to rentalId)).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Učitaj sve skenirane rentale pri startu
    suspend fun loadScannedRentals() {
        val adminUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val isAdmin = UserRepository.currentUser?.isAdmin ?: false
        if (!isAdmin) return

        try {
            val scannedIds = db.collection("users").document(adminUid)
                .collection("scannedRentals").get().await()

            for (doc in scannedIds.documents) {
                val rentalId = doc.getString("rentalId") ?: continue
                val rental = findRentalById(rentalId)
                if (rental != null) {
                    addScannedRental(rental)
                }
            }
            android.util.Log.d("RentalRepo", "Loaded ${_adminScannedRentals.size} scanned rentals for admin")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addScannedRental(rental: ActiveRental) {
        if (_adminScannedRentals.none { it.id == rental.id }) {
            _adminScannedRentals.add(rental)
        }
    }

    suspend fun addAndSaveScannedRental(rental: ActiveRental) {
        addScannedRental(rental)
        saveScannedRentalId(rental.id)
    }
}