package com.example.myapplication.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.example.myapplication.entity.User
import com.example.myapplication.entity.Page
import com.example.myapplication.entity.Course
import com.example.myapplication.entity.Progress
import com.example.myapplication.dao.UserDao
import com.example.myapplication.dao.PageDao
import com.example.myapplication.dao.CourseDao
import com.example.myapplication.dao.ProgressDao

@Database(
    entities = [User::class, Course::class, Page::class, Progress::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun pageDao(): PageDao
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}