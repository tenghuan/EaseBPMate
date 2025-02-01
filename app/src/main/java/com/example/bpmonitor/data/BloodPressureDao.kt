package com.example.bpmonitor.data

import androidx.room.*
import com.example.bpmonitor.model.BloodPressure
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodPressureDao {
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId ORDER BY measureDate DESC")
    fun getUserBPRecords(userId: Long): Flow<List<BloodPressure>>
    
    @Query("""
        SELECT * FROM blood_pressure 
        WHERE userId = :userId 
        AND date(datetime(measureDate/1000, 'unixepoch', 'localtime')) = 
            date(datetime(:measureDate/1000, 'unixepoch', 'localtime'))
    """)
    suspend fun getBPRecordByDate(userId: Long, measureDate: Long): BloodPressure?
    
    @Transaction
    suspend fun insertOrUpdateBP(bp: BloodPressure) {
        // 查找同一天的记录
        val existingRecord = getBPRecordByDate(bp.userId, bp.measureDate)
        if (existingRecord != null) {
            // 如果存在同一天的记录，删除它
            deleteBP(existingRecord)
        }
        // 插入新记录
        insertBP(bp)
    }
    
    @Insert
    suspend fun insertBP(bp: BloodPressure)
    
    @Delete
    suspend fun deleteBP(bp: BloodPressure)

    @Query("DELETE FROM blood_pressure WHERE userId = :userId")
    suspend fun deleteUserBPRecords(userId: Long)
} 