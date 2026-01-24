package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class AppContent(
    val appTitle: String,
    val menu: List<MenuNode>,
    val pages: Map<String, ContentDoc>
)

@Serializable
data class MenuNode(
    val title: String,
    val pageId: String? = null,
    val children: List<MenuNode>? = null
)

@Serializable
data class ContentDoc(
    val title: String,
    val blocks: List<ContentBlock>
)

@Serializable
data class ChartSeries(
    val name: String,
    val values: List<Float>
)

@Serializable
data class ContentBlock(
    val id: Int,
    val type: String,
    val text: String? = null,

    val url: String? = null,

    val autoplay: Boolean? = null,
    val loop: Boolean? = null,
    val showControls: Boolean? = null,

    val chartType: String? = null,
    val title: String? = null,
    val xLabels: List<String>? = null,
    val series: List<ChartSeries>? = null,

    val caption: String? = null
)
