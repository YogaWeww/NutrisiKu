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
    // Inisialisasi MainViewModel di sini
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tetap gunakan installSplashScreen untuk menangani cold start
        installSplashScreen().setKeepOnScreenCondition {
            mainViewModel.isLoading.value
        }

        setContent {
            NutrisiKuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Ambil start destination dari ViewModel
                    val startDestination by mainViewModel.startDestination.collectAsState()
                    val isLoading by mainViewModel.isLoading.collectAsState()

                    if (!isLoading && startDestination != null) {
                        // Kirim startDestination yang benar ke NutrisiKuApp
                        NutrisiKuApp(startDestination = startDestination!!)
                    } else {
                        // Selama loading, bisa tampilkan splash screen lagi jika perlu
                        // Namun, setKeepOnScreenCondition sudah menanganinya.
                        // Blok ini bisa dikosongkan atau diisi UI loading lain.
                    }
                }
            }
        }
    }
}