package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.data.db.AuthTokenEntity
import com.example.myapplication.data.db.DbProvider

class AuthRepository(context: Context) {
    private val dao = DbProvider.get(context).authTokenDao()

    suspend fun getToken(): String? = dao.get()?.accessToken

    suspend fun saveToken(token: String) {
        dao.upsert(AuthTokenEntity(accessToken = token))
    }

    suspend fun logout() {
        dao.clear()
    }
}
