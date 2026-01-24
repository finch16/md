package com.example.myapplication.ui

import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.model.ChartSeries
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter

@Composable
fun ChartBlock(
    chartType: String,
    title: String?,
    xLabels: List<String>?,
    series: List<ChartSeries>,
    modifier: Modifier = Modifier
) {
    Column {
        if (!title.isNullOrBlank()) {
            Text(title, style = MaterialTheme.typography.titleMedium)
        }

        when (chartType.lowercase()) {
            "bar" -> BarChartView(series, xLabels, modifier)
            else -> LineChartView(series, xLabels, modifier) // default "line"
        }
    }
}

@Composable
private fun LineChartView(
    series: List<ChartSeries>,
    xLabels: List<String>?,
    modifier: Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxWidth().height(260.dp),
        factory = { ctx ->
            LineChart(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)

                axisRight.isEnabled = false
                xAxis.granularity = 1f
                xAxis.valueFormatter = labelFormatter(xLabels)

                legend.isEnabled = true
            }
        },
        update = { chart ->
            val dataSets = series.mapIndexed { idx, s ->
                val entries = s.values.mapIndexed { x, y -> Entry(x.toFloat(), y) }
                LineDataSet(entries, s.name).apply {
                    setDrawValues(false)
                    setDrawCircles(true)
                    circleRadius = 3f
                    lineWidth = 2f
                }
            }
            chart.data = LineData(dataSets)
            chart.invalidate()
        }
    )
}

@Composable
private fun BarChartView(
    series: List<ChartSeries>,
    xLabels: List<String>?,
    modifier: Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxWidth().height(260.dp),
        factory = { ctx ->
            BarChart(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                setTouchEnabled(true)
                setScaleEnabled(true)

                axisRight.isEnabled = false
                xAxis.granularity = 1f
                xAxis.valueFormatter = labelFormatter(xLabels)

                legend.isEnabled = true
            }
        },
        update = { chart ->
            val barData = BarData()

            val dataSets = series.mapIndexed { sIdx, s ->
                val entries = s.values.mapIndexed { x, y -> BarEntry(x.toFloat(), y) }
                BarDataSet(entries, s.name).apply {
                    setDrawValues(false)
                }
            }

            dataSets.forEach { barData.addDataSet(it) }

            val groupCount = series.firstOrNull()?.values?.size ?: 0
            if (series.size > 1 && groupCount > 0) {
                val groupSpace = 0.2f
                val barSpace = 0.05f
                val barWidth = (1f - groupSpace) / series.size - barSpace
                barData.barWidth = barWidth

                chart.data = barData
                chart.xAxis.axisMinimum = 0f
                chart.xAxis.axisMaximum = groupCount.toFloat()
                chart.groupBars(0f, groupSpace, barSpace)
            } else {
                barData.barWidth = 0.6f
                chart.data = barData
            }

            chart.invalidate()
        }
    )
}

private fun labelFormatter(labels: List<String>?): ValueFormatter {
    return object : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val i = value.toInt()
            return if (labels != null && i in labels.indices) labels[i] else i.toString()
        }
    }
}
