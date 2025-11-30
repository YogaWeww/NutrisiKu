package com.example.nutrisiku.ui.screen.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R
import com.example.nutrisiku.ui.viewmodel.ManualFoodItem

/**
 * Kartu untuk menginput detail satu item makanan secara manual.
 *
 * @param item Data item makanan yang sedang diinput.
 * @param onItemChange Callback yang dipanggil saat ada perubahan pada data item.
 * @param onRemoveClick Callback yang dipanggil saat tombol hapus item diklik.
 * @param isLastItem Menandakan apakah ini adalah satu-satunya item di daftar (untuk menyembunyikan tombol hapus).
 */
@Composable
fun ManualFoodItemCard(
    item: ManualFoodItem,
    onItemChange: (ManualFoodItem) -> Unit,
    onRemoveClick: () -> Unit,
    isLastItem: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isLastItem) {
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.content_desc_delete_item),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            OutlinedTextField(
                value = item.foodName,
                onValueChange = { onItemChange(item.copy(foodName = it)) },
                label = { Text(stringResource(R.string.label_food_name) + "*") },
                leadingIcon = { Icon(Icons.Default.Fastfood, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = item.portion,
                onValueChange = { onItemChange(item.copy(portion = it)) },
                label = { Text(stringResource(R.string.portion_in_grams_label) + "*") },
                leadingIcon = { Icon(Icons.Default.Kitchen, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = item.calories,
                onValueChange = { onItemChange(item.copy(calories = it)) },
                label = { Text(stringResource(R.string.calories_per_portion_label) + "*") },
                leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.label_quantity), style = MaterialTheme.typography.bodyLarge)
                QuantityEditor(
                    quantity = item.quantity,
                    onDecrement = { onItemChange(item.copy(quantity = (item.quantity - 1).coerceAtLeast(1))) },
                    onIncrement = { onItemChange(item.copy(quantity = item.quantity + 1)) }
                )
            }
        }
    }
}

/**
 * Komponen untuk memilih gambar, menampilkan gambar yang dipilih atau placeholder.
 *
 * @param bitmap Bitmap gambar yang dipilih, bisa null.
 * @param onImageClick Aksi yang dipanggil saat komponen ini diklik untuk memilih gambar.
 */
@Composable
fun ImagePicker(
    bitmap: Bitmap?,
    onImageClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onImageClick),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.manual_input_image_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = stringResource(R.string.add_photo_button),
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    stringResource(R.string.add_photo_optional_label),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
