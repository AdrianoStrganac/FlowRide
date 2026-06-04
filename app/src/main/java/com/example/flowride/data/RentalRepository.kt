package com.example.flowride.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject

data class ActiveRental(
    val id: String,
    val vehicleType: String,
    val startDate: String,
    val endDate: String,
    val pickupLocation: String,
    val duration: String,
    val price: Int,
    val paymentMethod: String,
    val status: String = "active",
    val userName: String = "Nepoznato",
    val userEmail: String = ""
)

object RentalRepository {
    private val _rentals = mutableStateListOf<ActiveRental>()
    
    // Provide rentals for the currently logged in user
    val rentals: List<ActiveRental> get() {
        val email = UserRepository.currentUser?.email
        return if (email != null) {
            _rentals.filter { it.userEmail == email }
        } else {
            emptyList()
        }
    }

    // Admin view of all rentals
    val allRentalsForAdmin: List<ActiveRental> get() = _rentals

    private var prefs: android.content.SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.getSharedPreferences("flowride_rentals", Context.MODE_PRIVATE)
        loadRentals()
        
        // Initial mock data if empty
        if (_rentals.isEmpty()) {
            addRental(ActiveRental(
                id = "R001",
                vehicleType = "E-Bicikl",
                startDate = "02. 06. 2026.",
                endDate = "02. 06. 2026.",
                pickupLocation = "Central Park Station",
                duration = "8 sati",
                price = 200,
                paymentMethod = "card",
                status = "active",
                userName = "John Doe",
                userEmail = "john@example.com"
            ))
        }
    }

    private fun saveRentals() {
        val array = JSONArray()
        _rentals.forEach { rental ->
            val obj = JSONObject()
            obj.put("id", rental.id)
            obj.put("vehicleType", rental.vehicleType)
            obj.put("startDate", rental.startDate)
            obj.put("endDate", rental.endDate)
            obj.put("pickupLocation", rental.pickupLocation)
            obj.put("duration", rental.duration)
            obj.put("price", rental.price)
            obj.put("paymentMethod", rental.paymentMethod)
            obj.put("status", rental.status)
            obj.put("userName", rental.userName)
            obj.put("userEmail", rental.userEmail)
            array.put(obj)
        }
        prefs?.edit()?.putString("all_rentals_json", array.toString())?.apply()
    }

    private fun loadRentals() {
        val json = prefs?.getString("all_rentals_json", null) ?: return
        try {
            val array = JSONArray(json)
            _rentals.clear()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                _rentals.add(ActiveRental(
                    obj.getString("id"),
                    obj.getString("vehicleType"),
                    obj.getString("startDate"),
                    obj.getString("endDate"),
                    obj.getString("pickupLocation"),
                    obj.getString("duration"),
                    obj.getInt("price"),
                    obj.getString("paymentMethod"),
                    obj.getString("status"),
                    obj.getString("userName"),
                    obj.getString("userEmail")
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addRental(rental: ActiveRental) {
        _rentals.add(rental)
        saveRentals()
    }

    fun deleteRentalsForUser(email: String) {
        _rentals.removeIf { it.userEmail == email }
        saveRentals()
    }

    fun confirmRental(rentalId: String) {
        val index = _rentals.indexOfFirst { it.id == rentalId }
        if (index != -1) {
            val oldRental = _rentals[index]
            _rentals[index] = oldRental.copy(status = "completed")
            saveRentals()
        }
    }
}
