package com.example.nutrisiku.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Ekstensi untuk membuat instance DataStore tunggal untuk seluruh aplikasi
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

class UserRepository(private val context: Context) {

    // Definisikan key untuk setiap data yang akan disimpan
    private companion object {
        val NAME_KEY = stringPreferencesKey("user_name")
        val AGE_KEY = intPreferencesKey("user_age")
        val WEIGHT_KEY = doublePreferencesKey("user_weight")
        val HEIGHT_KEY = doublePreferencesKey("user_height")
        val GENDER_KEY = stringPreferencesKey("user_gender")
        val ACTIVITY_LEVEL_KEY = stringPreferencesKey("user_activity_level")
    }

    // Flow untuk mendapatkan data pengguna secara real-time
    val userDataFlow: Flow<UserData> = context.dataStore.data
        .map { preferences ->
            UserData(
                name = preferences[NAME_KEY] ?: "",
                age = preferences[AGE_KEY] ?: 0,
                weight = preferences[WEIGHT_KEY] ?: 0.0,
                height = preferences[HEIGHT_KEY] ?: 0.0,
                gender = preferences[GENDER_KEY] ?: "Pria",
                activityLevel = preferences[ACTIVITY_LEVEL_KEY] ?: "Aktivitas Ringan"
            )
        }

    // Fungsi untuk menyimpan data pengguna
    suspend fun saveUserData(userData: UserData) {
        context.dataStore.edit { preferences ->
            preferences[NAME_KEY] = userData.name
            preferences[AGE_KEY] = userData.age
            preferences[WEIGHT_KEY] = userData.weight
            preferences[HEIGHT_KEY] = userData.height
            preferences[GENDER_KEY] = userData.gender
            preferences[ACTIVITY_LEVEL_KEY] = userData.activityLevel
        }
    }
}
