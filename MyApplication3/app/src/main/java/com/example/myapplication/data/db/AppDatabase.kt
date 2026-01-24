package com.example.myapplication.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AuthTokenEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authTokenDao(): AuthTokenDao
}
