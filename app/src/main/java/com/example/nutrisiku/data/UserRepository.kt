package com.example.nutrisiku.data

import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Ekstensi untuk membuat instance DataStore tunggal untuk seluruh aplikasi
// Ganti nama DataStore agar lebih spesifik
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nutrisiku_prefs")

class UserRepository(private val context: Context) {

    private companion object {
        // Kunci untuk data profil pengguna
        val NAME_KEY = stringPreferencesKey("user_name")
        val AGE_KEY = intPreferencesKey("user_age")
        val WEIGHT_KEY = doublePreferencesKey("user_weight")
        val HEIGHT_KEY = doublePreferencesKey("user_height")
        val GENDER_KEY = stringPreferencesKey("user_gender")
        val ACTIVITY_LEVEL_KEY = stringPreferencesKey("user_activity_level")
        val IMAGE_PATH_KEY = stringPreferencesKey("user_image_path")

        // PERUBAHAN: Tambahkan key baru untuk status onboarding
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    }

    // Flow untuk mendapatkan data pengguna secara real-time (kode Anda dipertahankan)
    val userDataFlow: Flow<UserData> = context.dataStore.data
        .map { preferences ->
            UserData(
                name = preferences[NAME_KEY] ?: "",
                age = preferences[AGE_KEY] ?: 0,
                weight = preferences[WEIGHT_KEY] ?: 0.0,
                height = preferences[HEIGHT_KEY] ?: 0.0,
                gender = preferences[GENDER_KEY] ?: "Pria",
                activityLevel = preferences[ACTIVITY_LEVEL_KEY] ?: "Aktivitas Ringan",
                imagePath = preferences[IMAGE_PATH_KEY] ?: ""
            )
        }

    // PERUBAHAN: Flow baru untuk membaca status onboarding
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] ?: false
    }

    // Fungsi untuk menyimpan data pengguna (kode Anda dipertahankan)
    suspend fun saveUserData(userData: UserData) {
        context.dataStore.edit { preferences ->
            preferences[NAME_KEY] = userData.name
            preferences[AGE_KEY] = userData.age
            preferences[WEIGHT_KEY] = userData.weight
            preferences[HEIGHT_KEY] = userData.height
            preferences[GENDER_KEY] = userData.gender
            preferences[ACTIVITY_LEVEL_KEY] = userData.activityLevel
            preferences[IMAGE_PATH_KEY] = userData.imagePath
        }
    }

    // PERUBAHAN: Fungsi baru untuk menandai onboarding telah selesai
    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = true
        }
    }

    // Fungsi untuk menyimpan file gambar (kode Anda dipertahankan)
    suspend fun saveProfilePicture(bitmap: Bitmap): String? {
        return withContext(Dispatchers.IO) {
            val filename = "profile_picture.jpg"
            val file = File(context.filesDir, filename)
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                file.absolutePath
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }
}