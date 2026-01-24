package com.example.myapplication.data

import com.example.myapplication.model.AppContent
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

class ContentApi(private val baseUrl: String) {
    suspend fun fetchAppContent(courseSlug: String, token: String): AppContent {
        return ApiClient.http.get("$baseUrl/content") {
            header("Authorization", "Bearer $token")
            url { parameters.append("course", courseSlug) }
        }.body()
    }
}
