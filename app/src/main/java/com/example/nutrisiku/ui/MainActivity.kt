package com.example.nutrisiku.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.nutrisiku.ui.NutrisiKuApp // Import NutrisiKuApp
import com.example.nutrisiku.ui.screen.HomeScreen
import com.example.nutrisiku.ui.theme.NutrisiKuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            NutrisiKuTheme {
                // Panggil Composable utama yang berisi NavHost
                NutrisiKuApp()
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun HomeScreenPreview() {
//    NutrisiKuTheme {
//        HomeScreen()
//    }
//}