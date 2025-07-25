package com.example.nutrisiku.ui
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // Import ini
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.nutrisiku.ui.theme.NutrisiKuTheme
import com.example.nutrisiku.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Buat instance MainViewModel
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tetap tampilkan splash screen sampai kita selesai memeriksa data pengguna
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.isLoading.value
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            NutrisiKuTheme {
                // PERBAIKAN: Gunakan collectAsState untuk membaca StateFlow dengan aman
                val startDestination by viewModel.startDestination.collectAsState()

                // Panggil NutrisiKuApp dengan start destination yang benar
                NutrisiKuApp(startDestination = startDestination)
            }
        }
    }
}