package com.example.myapplication.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val photo: String
)

@Entity(tableName = "course")
data class Course(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "is_load")
    val isLoad: Boolean = false,
    val path: String
)

@Entity(tableName = "page")
data class Page(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "course_id")
    val courseId: Long,
    val parts: Int
)

@Entity(
    tableName = "progress",
    indices = [
        Index(value = ["user_id", "course_id"], unique = false),
        Index(value = ["user_id", "course_id", "page_id"], unique = true)
    ]
)
data class Progress(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "course_id")
    val courseId: Long,
    @ColumnInfo(name = "page_id")
    val pageId: Long,
    val pos: Int = 0,
    @ColumnInfo(name = "is_last")
    val isLast: Boolean = false
)