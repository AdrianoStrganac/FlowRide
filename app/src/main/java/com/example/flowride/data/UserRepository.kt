package com.example.flowride.data

import android.content.Context
import androidx.compose.runtime.*
import org.json.JSONArray
import org.json.JSONObject

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val profilePicture: String? = null,
    val isAdmin: Boolean = false
)

object UserRepository {
    private var _currentUser by mutableStateOf<UserProfile?>(null)
    val currentUser: UserProfile? get() = _currentUser

    private val users = mutableStateListOf<UserProfile>()
    private var prefs: android.content.SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.getSharedPreferences("flowride_prefs", Context.MODE_PRIVATE)
        
        loadUsers()
        
        // Ensure admin always exists in DB, but don't log them in
        if (users.none { it.email == "admin@flowride.com" }) {
            users.add(UserProfile("Admin User", "admin@flowride.com", "000", "HQ", isAdmin = true))
            saveUsers()
        }
        
        // Restore session ONLY if someone was previously logged in
        val savedEmail = prefs?.getString("current_user_email", null)
        if (savedEmail != null) {
            _currentUser = users.find { it.email == savedEmail }
        }
    }

    private fun saveUsers() {
        val array = JSONArray()
        users.forEach { user ->
            val obj = JSONObject()
            obj.put("name", user.name)
            obj.put("email", user.email)
            obj.put("phone", user.phone)
            obj.put("address", user.address)
            obj.put("isAdmin", user.isAdmin)
            array.put(obj)
        }
        prefs?.edit()?.putString("all_users_json", array.toString())?.apply()
    }

    private fun loadUsers() {
        val json = prefs?.getString("all_users_json", null) ?: return
        try {
            val array = JSONArray(json)
            users.clear()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                users.add(UserProfile(
                    obj.getString("name"),
                    obj.getString("email"),
                    obj.getString("phone"),
                    obj.getString("address"),
                    null,
                    obj.getBoolean("isAdmin")
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun login(email: String): Boolean {
        val user = users.find { it.email == email }
        return if (user != null) {
            _currentUser = user
            prefs?.edit()?.putString("current_user_email", email)?.apply()
            true
        } else {
            false
        }
    }

    fun register(name: String, email: String, phone: String, address: String) {
        if (users.any { it.email == email }) {
            login(email)
            return
        }
        
        val newUser = UserProfile(
            name = name,
            email = email,
            phone = phone,
            address = address,
            isAdmin = email.lowercase().contains("admin")
        )
        users.add(newUser)
        saveUsers()
        
        // Automatically login after successful registration
        _currentUser = newUser
        prefs?.edit()?.putString("current_user_email", email)?.apply()
    }

    fun logout() {
        _currentUser = null
        prefs?.edit()?.remove("current_user_email")?.apply()
    }

    fun deleteAccount() {
        val email = _currentUser?.email
        if (email != null) {
            users.removeIf { it.email == email }
            // Clean up rentals for this user
            RentalRepository.deleteRentalsForUser(email)
            saveUsers()
        }
        logout()
    }
}
