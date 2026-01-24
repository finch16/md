package com.example.myapplication.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_tokens")
data class AuthTokenEntity(
    @PrimaryKey val id: Int = 1,
    val accessToken: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)
