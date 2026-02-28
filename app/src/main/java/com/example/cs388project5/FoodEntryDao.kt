package com.example.cs388project5

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {
    @Query("SELECT * FROM food_entries ORDER BY timestamp DESC")
    fun getAll(): Flow<List<FoodEntryEntity>>

    @Insert
    suspend fun insert(entry: FoodEntryEntity)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM food_entries")
    suspend fun deleteAll()
}
