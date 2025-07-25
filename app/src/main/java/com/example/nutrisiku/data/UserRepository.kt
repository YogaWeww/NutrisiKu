package com.example.nutrisiku.data

import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
        val IMAGE_PATH_KEY = stringPreferencesKey("user_image_path") // PERUBAHAN: Tambahkan key baru
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
                activityLevel = preferences[ACTIVITY_LEVEL_KEY] ?: "Aktivitas Ringan",
                imagePath = preferences[IMAGE_PATH_KEY] ?: "" // PERUBAHAN: Baca path gambar
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
            preferences[IMAGE_PATH_KEY] = userData.imagePath // PERUBAHAN: Simpan path gambar
        }
    }

    // PERUBAHAN: Fungsi baru untuk menyimpan file gambar
    suspend fun saveProfilePicture(bitmap: Bitmap): String? {
        return withContext(Dispatchers.IO) {
            val filename = "profile_picture.jpg" // Gunakan nama file yang tetap agar mudah ditimpa
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
