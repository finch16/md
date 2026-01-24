package com.example.myapplication.data

import com.example.myapplication.model.CourseItem

class CoursesRepository(private val api: CoursesApi) {
    suspend fun getCourses(token: String): List<CourseItem> {
        return api.fetchCourses(token).items
    }
}
