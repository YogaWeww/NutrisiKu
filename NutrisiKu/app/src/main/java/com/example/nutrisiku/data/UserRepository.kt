package com.example.nutrisiku.data

import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Membuat extension property untuk Context agar DataStore menjadi singleton di seluruh aplikasi.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nutrisiku_prefs")

/**
 * Repository untuk mengelola data profil pengguna dan preferensi aplikasi.
 * Menggunakan Jetpack DataStore untuk persistensi data key-value secara asinkron.
 *
 * @param context Konteks aplikasi, digunakan untuk mengakses DataStore.
 */
class UserRepository(private val context: Context) {

    /**
     * Companion object untuk menampung semua kunci (keys) yang digunakan di DataStore.
     * Mengelompokkan kunci di satu tempat membuatnya lebih mudah dikelola dan mencegah kesalahan pengetikan.
     */
    private companion object {
        val NAME_KEY = stringPreferencesKey("user_name")
        val AGE_KEY = intPreferencesKey("user_age")
        val WEIGHT_KEY = doublePreferencesKey("user_weight")
        val HEIGHT_KEY = doublePreferencesKey("user_height")
        val GENDER_KEY = stringPreferencesKey("user_gender")
        val ACTIVITY_LEVEL_KEY = stringPreferencesKey("user_activity_level")
        val IMAGE_PATH_KEY = stringPreferencesKey("user_image_path")
        val TDEE_KEY = floatPreferencesKey("user_tdee")
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    }

    /**
     * Flow yang memancarkan objek [UserData] setiap kali ada perubahan pada preferensi pengguna.
     * Dilengkapi dengan penanganan eror untuk mencegah aplikasi crash jika terjadi IOException saat membaca data.
     */
    val userDataFlow: Flow<UserData> = context.dataStore.data
        .catch { exception ->
            // Jika terjadi eror saat membaca DataStore (misal: file rusak)
            if (exception is IOException) {
                // Pancarkan nilai default yang aman untuk mencegah crash
                emit(emptyPreferences())
            } else {
                // Lemparkan kembali eror lain yang tidak terduga
                throw exception
            }
        }
        .map { preferences ->
            // Memetakan objek Preferences menjadi objek UserData
            UserData(
                name = preferences[NAME_KEY] ?: "",
                age = preferences[AGE_KEY] ?: 0,
                weight = preferences[WEIGHT_KEY] ?: 0.0,
                height = preferences[HEIGHT_KEY] ?: 0.0,
                gender = preferences[GENDER_KEY] ?: "Pria",
                activityLevel = preferences[ACTIVITY_LEVEL_KEY] ?: "Aktivitas Ringan",
                imagePath = preferences[IMAGE_PATH_KEY] ?: "",
                tdee = preferences[TDEE_KEY] ?: 0f
            )
        }

    /**
     * Flow yang memancarkan status apakah pengguna telah menyelesaikan proses onboarding.
     * Juga dilengkapi dengan penanganan eror.
     */
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] ?: false
        }

    /**
     * Menyimpan seluruh objek [UserData] ke DataStore.
     * @param userData Objek UserData yang akan disimpan.
     */
    suspend fun saveUserData(userData: UserData) {
        context.dataStore.edit { preferences ->
            preferences[NAME_KEY] = userData.name
            preferences[AGE_KEY] = userData.age
            preferences[WEIGHT_KEY] = userData.weight
            preferences[HEIGHT_KEY] = userData.height
            preferences[GENDER_KEY] = userData.gender
            preferences[ACTIVITY_LEVEL_KEY] = userData.activityLevel
            preferences[IMAGE_PATH_KEY] = userData.imagePath
            preferences[TDEE_KEY] = userData.tdee
        }
    }

    /**
     * Menandai bahwa proses onboarding telah selesai.
     */
    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = true
        }
    }

    /**
     * Menyimpan gambar profil (dalam format Bitmap) ke penyimpanan internal aplikasi.
     * @param bitmap Gambar yang akan disimpan.
     * @return Path absolut dari file gambar yang disimpan, atau null jika gagal.
     */
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

    /**
     * Menghapus path gambar profil dari DataStore, yang akan membuat UI
     * menampilkan gambar default.
     */
    suspend fun deleteProfilePicture() {
        context.dataStore.edit { preferences ->
            preferences[IMAGE_PATH_KEY] = ""
        }
    }
}
