package com.example.myapplication.data

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(
    val access_token: String,
    val token_type: String? = null,
    val expires_in: Long? = null
)

class AuthApi(private val baseUrl: String) {
    suspend fun login(username: String, password: String): String {
        val resp: LoginResponse = ApiClient.http.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }.body()
        return resp.access_token
    }
}
