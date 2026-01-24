package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.model.AppContent
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

fun loadAppContent(context: Context): AppContent {
    val raw = context.assets.open("app_content.json")
        .bufferedReader()
        .use { it.readText() }

    return json.decodeFromString(AppContent.serializer(), raw)
}
