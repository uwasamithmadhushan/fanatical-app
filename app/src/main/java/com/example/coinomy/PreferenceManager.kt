package com.example.coinomy

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class PreferenceManager private constructor(context: Context) {

    companion object {
        private const val PREFS_NAME = "coinomy_preferences"
        private const val KEY_FIRST_RUN = "is_first_run"
        private const val PREF_IS_LOGGED_IN = "pref_is_logged_in"
        private const val PREF_USER_PASSWORD = "pref_user_password"
        private const val PREF_REMEMBER_ME = "pref_remember_me"
        private const val TAG = "PreferenceManager"

        @Volatile
        private var instance: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager {
            return instance ?: synchronized(this) {
                instance ?: PreferenceManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Check if this is the first run of the app
    fun isFirstRun(): Boolean {
        return try {
            prefs.getBoolean(KEY_FIRST_RUN, true)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking first run status", e)
            true // Default to true on error
        }
    }

    // Set first run status
    fun setFirstRun(isFirstRun: Boolean) {
        try {
            prefs.edit().putBoolean(KEY_FIRST_RUN, isFirstRun).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting first run status", e)
        }
    }

    // Get user name
    fun getUserName(): String {
        return try {
            prefs.getString("user_name", "User") ?: "User"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user name", e)
            "User" // Default value on error
        }
    }

    // Save user name
    fun setUserName(name: String) {
        try {
            prefs.edit().putString("user_name", name).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting user name", e)
        }
    }

    // Get monthly budget
    fun getMonthlyBudget(): Double {
        return try {
            prefs.getFloat("monthly_budget", 2500f).toDouble()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting monthly budget", e)
            2500.0 // Default value on error
        }
    }

    // Set monthly budget
    fun setMonthlyBudget(budget: Double) {
        try {
            prefs.edit().putFloat("monthly_budget", budget.toFloat()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting monthly budget", e)
        }
    }

    // Login related methods
    fun isLoggedIn(): Boolean {
        return try {
            prefs.getBoolean(PREF_IS_LOGGED_IN, false)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking login status", e)
            false // Default to false on error
        }
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        try {
            prefs.edit().putBoolean(PREF_IS_LOGGED_IN, isLoggedIn).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting login status", e)
        }
    }

    fun getUserPassword(): String? {
        return try {
            prefs.getString(PREF_USER_PASSWORD, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user password", e)
            null // Default to null on error
        }
    }

    fun setUserPassword(password: String) {
        try {
            prefs.edit().putString(PREF_USER_PASSWORD, password).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting user password", e)
        }
    }

    fun getRememberMe(): Boolean {
        return try {
            prefs.getBoolean(PREF_REMEMBER_ME, false)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting remember me status", e)
            false // Default to false on error
        }
    }

    fun setRememberMe(rememberMe: Boolean) {
        try {
            prefs.edit().putBoolean(PREF_REMEMBER_ME, rememberMe).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting remember me status", e)
        }
    }

    // Clear all preferences
    fun clearAllPreferences() {
        try {
            prefs.edit().clear().apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing preferences", e)
        }
    }

    // Check if a preference exists
    fun contains(key: String): Boolean {
        return try {
            prefs.contains(key)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if preference exists", e)
            false // Default to false on error
        }
    }
}