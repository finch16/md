package com.example.myapplication.data

import com.example.myapplication.model.AppContent

class ContentRepository(
    private val api: ContentApi
) {
    private var cached: AppContent? = null

    suspend fun getAppContent(
        courseSlug: String,
        token: String,
        forceRefresh: Boolean = false
    ): AppContent {
        if (!forceRefresh) cached?.let { return it }

        return api.fetchAppContent(
            courseSlug = courseSlug,
            token = token
        ).also { cached = it }
    }
}
