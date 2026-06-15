package com.example.flowride.data

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PropertyName
import kotlinx.coroutines.tasks.await

data class BikeModelFirestore(
    val id: String = "",
    val name: String = "",
    val emoji: String = "",
    val pricePerHour: Int = 0,
    val description: String = "",
    val imageUrl: String = "",
    val features: List<String> = emptyList(),
    val categoryId: String = "",
    @get:PropertyName("isAvailable")
    @set:PropertyName("isAvailable")
    var isAvailable: Boolean = true
)

data class BikeCategoryFirestore(
    val id: String = "",
    val name: String = "",
    val emoji: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val order: Int = 0
)

object VehicleRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }

    private val _vehicles = mutableStateListOf<BikeModelFirestore>()
    val vehicles: List<BikeModelFirestore> get() = _vehicles

    private val _categories = mutableStateListOf<BikeCategoryFirestore>()
    val categories: List<BikeCategoryFirestore> get() = _categories

    // Čuvamo listener da ga možemo ukloniti
    private var vehiclesListener: ListenerRegistration? = null

    // VehicleRepository.kt — dodaj:
    private val _pendingToggles = androidx.compose.runtime.mutableStateMapOf<String, Boolean>()

    fun getEffectiveAvailability(vehicleId: String): Boolean {
        return _pendingToggles[vehicleId]
            ?: _vehicles.find { it.id == vehicleId }?.isAvailable
            ?: true
    }

    suspend fun toggleAvailability(vehicleId: String, newStatus: Boolean) {
        android.util.Log.d("VehicleRepo", "toggleAvailability: id=$vehicleId, newStatus=$newStatus")
        _pendingToggles[vehicleId] = newStatus
        try {
            db.collection("vehicles").document(vehicleId)
                .update("isAvailable", newStatus).await()
            android.util.Log.d("VehicleRepo", "Firestore update success")
            // NE uklanjamo iz _pendingToggles ovdje
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Firestore update FAILED: ${e.message}", e)
            _pendingToggles.remove(vehicleId) // ukloni samo ako greška
        }
    }

    fun startVehiclesListener() {
        vehiclesListener?.remove()
        vehiclesListener = db.collection("vehicles")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("VehicleRepo", "Listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _vehicles.clear()
                    _vehicles.addAll(snapshot.toObjects(BikeModelFirestore::class.java))
                    // Ukloni pending toggles za vozila čiji status je potvrđen iz Firestorea
                    _vehicles.forEach { vehicle ->
                        val pending = _pendingToggles[vehicle.id]
                        if (pending != null && pending == vehicle.isAvailable) {
                            _pendingToggles.remove(vehicle.id)
                        }
                    }
                    android.util.Log.d("VehicleRepo", "Real-time update: ${_vehicles.size} vozila")
                }
            }
    }

    fun stopVehiclesListener() {
        vehiclesListener?.remove()
        vehiclesListener = null
    }

    // Zadržavamo loadVehicles za inicijalnu provjeru je li lista prazna
    suspend fun loadVehicles() {
        try {
            val snapshot = db.collection("vehicles").get().await()
            _vehicles.clear()
            _vehicles.addAll(snapshot.toObjects(BikeModelFirestore::class.java))
            android.util.Log.d("VehicleRepo", "Loaded ${_vehicles.size} vehicles")
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error: ${e.message}", e)
        }
    }

    suspend fun loadCategories() {
        try {
            val snapshot = db.collection("categories").orderBy("order").get().await()
            _categories.clear()
            _categories.addAll(snapshot.toObjects(BikeCategoryFirestore::class.java))
            android.util.Log.d("VehicleRepo", "Loaded ${_categories.size} categories")
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error loading categories: ${e.message}", e)
        }
    }

    suspend fun saveVehicle(vehicle: BikeModelFirestore) {
        try {
            db.collection("vehicles").document(vehicle.id).set(vehicle).await()
            // Listener će automatski osvježiti listu
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteVehicle(vehicleId: String) {
        try {
            db.collection("vehicles").document(vehicleId).delete().await()
            // Listener će automatski osvježiti listu
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getVehiclesForCategory(
        categoryId: String,
        onlyAvailable: Boolean = false
    ): List<BikeModelFirestore> {
        val filtered = _vehicles.filter { it.categoryId == categoryId }
        return if (onlyAvailable) {
            filtered.filter { getEffectiveAvailability(it.id) }
        } else {
            filtered
        }
    }
}