package com.mekki.taco.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.DecimalFormat

data class PieChartData(val value: Float, val color: Color, val label: String)

@Composable
fun MacroPieChart(
    modifier: Modifier = Modifier,
    data: List<PieChartData>,
    totalValue: Double,
    totalUnit: String
) {
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    if (total == 0f) return // Avoid division by zero

    // Calculates angles for slices
    val angles = data.map { 360f * it.value / total }
    // Animate each slice
    val animatedProgress = remember { angles.map { Animatable(0f) } }

    LaunchedEffect(angles) {
        animatedProgress.forEachIndexed { index, animatable ->
            launch {
                animatable.animateTo(
                    targetValue = angles[index],
                    animationSpec = tween(durationMillis = 800, delayMillis = index * 150)
                )
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pie Chart with center label
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(200.dp)) {
                var startAngle = -90f
                animatedProgress.forEachIndexed { index, animatable ->
                    drawArc(
                        color = data[index].color,
                        startAngle = startAngle,
                        sweepAngle = animatable.value,
                        useCenter = false,
                        style = Stroke(width = 60f, cap = Stroke.DefaultCap)
                    )
                    startAngle += animatable.value
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val df = DecimalFormat("#.##")
                Text(
                    text = df.format(totalValue),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = totalUnit,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Column to store the chart's Legend
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.forEach { slice ->
                val percentage = if (total > 0) DecimalFormat("#.##").format(100 * slice.value / total) else "0"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color = slice.color, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${slice.label}: $percentage%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
