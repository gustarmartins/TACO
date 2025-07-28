package com.mekki.taco.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
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

    val angles = data.map { 360f * it.value / total }
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

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(200.dp)) {
            var startAngle = -90f
            animatedProgress.forEachIndexed { index, animatable ->
                drawArc(
                    color = data[index].color,
                    startAngle = startAngle,
                    sweepAngle = animatable.value,
                    useCenter = false,
                    style = Stroke(width = 60f)
                )
                startAngle += animatable.value
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val df = DecimalFormat("#.#")
            Text(
                text = df.format(totalValue),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = totalUnit,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
