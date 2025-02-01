package com.example.bpmonitor.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "blood_pressure",
    indices = [
        Index("userId")  // 添加索引
    ],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BloodPressure(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val systolic: Int, // 收缩压（高压）
    val diastolic: Int, // 舒张压（低压）
    val measureDate: Long,
    val isAbnormal: Boolean = false
) {
    companion object {
        const val SYSTOLIC_MAX = 140 // 正常收缩压上限
        const val SYSTOLIC_MIN = 90  // 正常收缩压下限
        const val DIASTOLIC_MAX = 90 // 正常舒张压上限
        const val DIASTOLIC_MIN = 60 // 正常舒张压下限
    }
} 