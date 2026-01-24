package com.example.myapplication.data

import com.example.myapplication.model.CoursesResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

class CoursesApi(private val baseUrl: String) {
    suspend fun fetchCourses(token: String): CoursesResponse {
        return ApiClient.http.get("$baseUrl/courses") {
            header("Authorization", "Bearer $token")
        }.body()
    }
}
