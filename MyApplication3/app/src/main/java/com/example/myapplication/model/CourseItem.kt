package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class CourseItem(
    val slug: String,
    val title: String,
    val language: String? = null
)

@Serializable
data class CoursesResponse(
    val items: List<CourseItem>
)
