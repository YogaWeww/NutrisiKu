package com.example.nutrisiku.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object ProfileInput : Screen("profile_input")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Detection : Screen("detection")
    object DetectionResult : Screen("detection_result")
    object ManualInput : Screen("manual_input")
    object History : Screen("history")
    object Camera : Screen("camera")
    object HistoryDetail : Screen("history_detail/{historyId}") {
        fun createRoute(historyId: Int) = "history_detail/$historyId"
    }
}