package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TechnicianEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TechnicianDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTechnician(technician: TechnicianEntity): Long

    @Update
    suspend fun updateTechnician(technician: TechnicianEntity)

    @Delete
    suspend fun deleteTechnician(technician: TechnicianEntity)

    @Query("SELECT * FROM technicians WHERE id = :id")
    suspend fun getTechnicianById(id: Long): TechnicianEntity?

    @Query("SELECT * FROM technicians")
    fun getAllTechnicians(): Flow<List<TechnicianEntity>>

    @Query("SELECT * FROM technicians WHERE isAvailable = 1")
    fun getAvailableTechnicians(): Flow<List<TechnicianEntity>>
}
