package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.model.ContentBlock
import com.example.myapplication.model.ContentDoc

@Composable
fun ContentScreen(doc: ContentDoc, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(doc.blocks, key = { it.id }) { block ->
            RenderBlock(block)
        }
    }
}

@Composable
private fun RenderBlock(block: ContentBlock) {
    when (block.type) {
        "h1" -> Text(block.text.orEmpty(), style = MaterialTheme.typography.headlineMedium)
        "h2" -> Text(block.text.orEmpty(), style = MaterialTheme.typography.headlineSmall)
        "p"  -> Text(block.text.orEmpty(), style = MaterialTheme.typography.bodyLarge)

        "img" -> {
            val url = block.url ?: return
            AsyncImage(
                model = url,
                contentDescription = block.caption,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            block.caption?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }

        "video" -> {
            val url = block.url ?: return
            NetworkVideoPlayer(
                url = url,
                autoplay = block.autoplay ?: false,
                loop = block.loop ?: false,
                showControls = block.showControls ?: true
            )
            block.caption?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }

        "chart" -> {
            val chartType = block.chartType ?: "line"
            val series = block.series ?: return

            ChartBlock(
                chartType = chartType,
                title = block.title,
                xLabels = block.xLabels,
                series = series
            )

            block.caption?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
