package com.example.nutrisiku.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutrisiku.ui.theme.NutrisiKuTheme
import com.example.nutrisiku.ui.viewmodel.MainViewModel
import com.example.nutrisiku.ui.viewmodel.ViewModelFactory

/**
 * Activity utama dan satu-satunya titik masuk (entry point) untuk aplikasi NutrisiKu.
 * Bertanggung jawab untuk:
 * 1. Menginisialisasi MainViewModel.
 * 2. Menangani logika Splash Screen.
 * 3. Mengatur tema dan konten utama aplikasi menggunakan Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    // Menginisialisasi MainViewModel menggunakan delegasi `by viewModels`
    // dengan ViewModelFactory kustom kita untuk menyediakan dependensi yang diperlukan.
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menginstal Splash Screen API.
        installSplashScreen().apply {
            // Menahan splash screen agar tetap tampil selama MainViewModel masih dalam
            // status loading (menentukan rute awal). Ini mencegah "kedipan" atau
            // blank screen saat aplikasi pertama kali dibuka.
            setKeepOnScreenCondition {
                mainViewModel.isLoading.value
            }
        }

        setContent {
            NutrisiKuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Mengamati (collect) startDestination dari ViewModel dengan cara yang
                    // sadar akan lifecycle (lifecycle-aware).
                    val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()

                    // Menunggu hingga startDestination memiliki nilai (tidak null) sebelum
                    // merender UI utama. Ini memastikan aplikasi tahu ke mana harus
                    // menavigasi pengguna pertama kali.
                    startDestination?.let { destination ->
                        // Jika tujuan sudah ditentukan dan tidak kosong, tampilkan aplikasi.
                        if (destination.isNotEmpty()) {
                            NutrisiKuApp(startDestination = destination)
                        }
                    }
                }
            }
        }
    }
}
