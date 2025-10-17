package com.example.nutrisiku.ui.navigation

/**
 * Sealed class yang mendefinisikan semua rute navigasi dalam aplikasi.
 * Menggunakan sealed class memastikan type-safety saat bernavigasi.
 *
 * @property route String unik yang merepresentasikan tujuan navigasi.
 */
sealed class Screen(val route: String) {
    /**
     * Layar onboarding yang ditampilkan pada pengguna baru.
     * Ini akan menjadi salah satu rute awal yang mungkin.
     */
    object Onboarding : Screen("onboarding")

    /**
     * Layar untuk input data profil pertama kali setelah onboarding.
     */
    object ProfileInput : Screen("profile_input")

    /**
     * Layar utama aplikasi (Home).
     * Ini akan menjadi rute awal lainnya yang mungkin.
     */
    object Home : Screen("home")

    /**
     * Layar untuk menampilkan dan mengelola profil pengguna.
     */
    object Profile : Screen("profile")

    /**
     * Layar untuk mengedit data profil pengguna.
     */
    object EditProfile : Screen("edit_profile")

    /**
     * Layar deteksi makanan menggunakan kamera real-time.
     */
    object Detection : Screen("detection")

    /**
     * Layar untuk menampilkan hasil deteksi setelah gambar dipilih atau dikonfirmasi.
     */
    object DetectionResult : Screen("detection_result")

    /**
     * Layar untuk input data makanan secara manual.
     */
    object ManualInput : Screen("manual_input")

    /**
     * Layar untuk menampilkan daftar riwayat konsumsi makanan.
     */
    object History : Screen("history")

    /**
     * Layar untuk menampilkan detail dari satu entri riwayat.
     * Membutuhkan ID riwayat sebagai argumen.
     */
    object HistoryDetail : Screen("history_detail/{historyId}") {
        fun createRoute(historyId: Int) = "history_detail/$historyId"
    }

    /**
     * Layar untuk mengedit satu entri riwayat.
     * Membutuhkan ID riwayat sebagai argumen.
     */
    object EditHistory : Screen("edit_history/{historyId}") {
        fun createRoute(historyId: Int) = "edit_history/$historyId"
    }
}

