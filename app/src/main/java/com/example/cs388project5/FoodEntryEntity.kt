package com.example.cs388project5

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_entries")
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "calories") val calories: Int,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "photoUri") val photoUri: String? = null
) {
    companion object {
        fun create(name: String, calories: Int, photoUri: String? = null): FoodEntryEntity {
            return FoodEntryEntity(0, name, calories, System.currentTimeMillis(), photoUri)
        }
    }
}