package com.example.nutrisiku.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R
import com.example.nutrisiku.data.HistoryFoodItem

/**
 * Komponen Composable untuk menampilkan satu item makanan dalam mode "baca-saja".
 * Digunakan di layar Detail Riwayat.
 *
 * @param foodItem Data item makanan yang akan ditampilkan.
 */
@Composable
fun ReadOnlyHistoryItem(foodItem: HistoryFoodItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kolom untuk nama dan detail porsi
        Column(modifier = Modifier.weight(1f)) {
            val displayName = if (foodItem.quantity > 1) {
                "${foodItem.name} x${foodItem.quantity}"
            } else {
                foodItem.name
            }
            Text(displayName, style = MaterialTheme.typography.bodyLarge)

            val portionText = stringResource(R.string.portion_per_item_format, foodItem.portion)
            Text(
                text = portionText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        // Teks untuk total kalori item
        val totalCalorieText = stringResource(R.string.kcal_value, foodItem.calories * foodItem.quantity)
        Text(totalCalorieText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
