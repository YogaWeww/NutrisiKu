package com.example.nutrisiku.ui
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.nutrisiku.ui.theme.NutrisiKuTheme
import com.example.nutrisiku.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().setKeepOnScreenCondition {
            viewModel.isLoading.value
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Panggil tema kustom Anda
            NutrisiKuTheme {
                // --- PERBAIKAN KUNCI ADA DI SINI ---
                // Bungkus seluruh aplikasi di dalam Surface.
                // Ini memaksa Compose untuk menggunakan warna dari colorScheme Anda
                // sebagai dasar untuk semua komponen di dalamnya.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background // Gunakan warna latar belakang dari tema Anda
                ) {
                    val startDestination by viewModel.startDestination.collectAsState()
                    NutrisiKuApp(startDestination = startDestination)
                }
            }
        }
    }
}
