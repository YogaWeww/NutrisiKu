package com.example.nutrisiku.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R
import com.example.nutrisiku.data.HistoryFoodItem

/**
 * Komponen Composable untuk menampilkan satu item makanan dalam mode edit di layar Edit Riwayat.
 * Menyediakan field untuk mengubah nama, porsi, kalori, dan kuantitas.
 *
 * @param item Data item makanan yang akan ditampilkan dan diedit.
 * @param onNameChange Callback yang dipanggil saat nama makanan diubah.
 * @param onPortionChange Callback yang dipanggil saat porsi diubah.
 * @param onCaloriesChange Callback yang dipanggil saat kalori diubah.
 * @param onQuantityChange Callback yang dipanggil saat kuantitas diubah.
 * @param onDeleteItem Callback yang dipanggil saat tombol hapus item diklik.
 */
@Composable
fun EditableHistoryItem(
    item: HistoryFoodItem,
    onNameChange: (String) -> Unit,
    onPortionChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onDeleteItem: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Baris untuk input nama makanan dan tombol hapus
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = item.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.label_food_name)) },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDeleteItem) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.content_desc_delete),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Baris untuk input porsi dan editor kuantitas
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = if (item.portion > 0) item.portion.toString() else "",
                onValueChange = onPortionChange,
                label = { Text(stringResource(R.string.portion_in_grams_label)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.width(16.dp))
            QuantityEditor(
                quantity = item.quantity,
                onDecrement = { onQuantityChange(item.quantity - 1) },
                onIncrement = { onQuantityChange(item.quantity + 1) }
            )
        }

        // Input untuk kalori per porsi
        OutlinedTextField(
            value = if (item.calories > 0) item.calories.toString() else "",
            onValueChange = onCaloriesChange,
            label = { Text(stringResource(R.string.calories_per_portion_label)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

