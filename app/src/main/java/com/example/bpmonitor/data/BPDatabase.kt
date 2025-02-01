package com.example.bpmonitor.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bpmonitor.model.User
import com.example.bpmonitor.model.BloodPressure

@Database(entities = [User::class, BloodPressure::class], version = 1)
abstract class BPDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bloodPressureDao(): BloodPressureDao
} 