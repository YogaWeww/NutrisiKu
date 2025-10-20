package com.example.nutrisiku.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R
import com.example.nutrisiku.ui.viewmodel.DetectedFoodItem

/**
 * Kartu untuk menampilkan satu item makanan hasil deteksi di [DetectionResultScreen].
 * Termasuk nama, porsi yang bisa diedit, editor kuantitas, dan total kalori per item.
 *
 * @param item Data item makanan yang akan ditampilkan.
 * @param onQuantityChange Callback yang dipanggil saat kuantitas diubah.
 * @param onPortionChange Callback yang dipanggil saat ikon edit porsi diklik.
 */
@Composable
fun FoodItemResultCard(
    item: DetectedFoodItem,
    onQuantityChange: (Int) -> Unit,
    onPortionChange: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.portion_value_g, item.standardPortion),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    IconButton(onClick = onPortionChange, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.content_desc_edit_portion),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            QuantityEditor(
                quantity = item.quantity,
                onDecrement = { onQuantityChange(item.quantity - 1) },
                onIncrement = { onQuantityChange(item.quantity + 1) }
            )

            Text(
                text = stringResource(R.string.kcal_value, item.caloriesPerPortion * item.quantity),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}
