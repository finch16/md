package com.example.myapplication.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AuthTokenDao {
    @Query("SELECT * FROM auth_tokens WHERE id = 1 LIMIT 1")
    suspend fun get(): AuthTokenEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(token: AuthTokenEntity)

    @Query("DELETE FROM auth_tokens")
    suspend fun clear()
}
