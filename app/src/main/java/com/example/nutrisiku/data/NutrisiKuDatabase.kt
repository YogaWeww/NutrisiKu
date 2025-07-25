package com.example.nutrisiku.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
@TypeConverters(FoodItemConverter::class)
abstract class NutrisiKuDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: NutrisiKuDatabase? = null

        fun getDatabase(context: Context): NutrisiKuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NutrisiKuDatabase::class.java,
                    "nutrisiku_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}