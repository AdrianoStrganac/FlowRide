package com.example.flowride.data

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
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
    val isAvailable: Boolean = true
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

    suspend fun loadVehicles() {
        try {
            android.util.Log.d("VehicleRepo", "Starting loadVehicles...")
            val snapshot = db.collection("vehicles").get().await()
            android.util.Log.d("VehicleRepo", "Got ${snapshot.size()} documents")
            snapshot.documents.forEach { doc ->
                android.util.Log.d("VehicleRepo", "Doc: ${doc.id} -> ${doc.data}")
            }
            _vehicles.clear()
            _vehicles.addAll(snapshot.toObjects(BikeModelFirestore::class.java))
            android.util.Log.d("VehicleRepo", "Mapped ${_vehicles.size} vehicles")
            _vehicles.forEach { v ->
                android.util.Log.d("VehicleRepo", "Vehicle: ${v.id} - ${v.name} - category: ${v.categoryId}")
            }
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error: ${e.message}", e)
        }
    }

    suspend fun loadCategories() {
        try {
            android.util.Log.d("VehicleRepo", "Starting loadCategories...")
            val snapshot = db.collection("categories").orderBy("order").get().await()
            android.util.Log.d("VehicleRepo", "Got ${snapshot.size()} categories")
            _categories.clear()
            _categories.addAll(snapshot.toObjects(BikeCategoryFirestore::class.java))
            android.util.Log.d("VehicleRepo", "Loaded categories: ${_categories.size}")
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error loading categories: ${e.message}", e)
        }
    }

    suspend fun saveVehicle(vehicle: BikeModelFirestore) {
        try {
            db.collection("vehicles").document(vehicle.id).set(vehicle).await()
            val index = _vehicles.indexOfFirst { it.id == vehicle.id }
            if (index != -1) {
                _vehicles[index] = vehicle
            } else {
                _vehicles.add(vehicle)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteVehicle(vehicleId: String) {
        try {
            db.collection("vehicles").document(vehicleId).delete().await()
            _vehicles.removeIf { it.id == vehicleId }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun toggleAvailability(vehicleId: String, currentStatus: Boolean) {
        try {
            val newStatus = !currentStatus
            db.collection("vehicles").document(vehicleId).update("isAvailable", newStatus).await()
            val index = _vehicles.indexOfFirst { it.id == vehicleId }
            if (index != -1) {
                _vehicles[index] = _vehicles[index].copy(isAvailable = newStatus)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getVehiclesForCategory(categoryId: String, onlyAvailable: Boolean = false): List<BikeModelFirestore> {
        return if (onlyAvailable) {
            _vehicles.filter { it.categoryId == categoryId && it.isAvailable }
        } else {
            _vehicles.filter { it.categoryId == categoryId }
        }
    }
}