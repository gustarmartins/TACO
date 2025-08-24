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
import androidx.compose.ui.graphics.StrokeCap
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
    if (total <= 0f) return // nothing to draw

    val angles = data.map { 360f * it.value / total }
    // animate each slice of the pie
    val animatedProgress = remember(data) { data.map { Animatable(0f) } }

    LaunchedEffect(data) {
        animatedProgress.forEachIndexed { index, animatable ->
            launch {
                animatable.animateTo(
                    targetValue = angles.getOrNull(index) ?: 0f,
                    animationSpec = tween(durationMillis = 700, delayMillis = index * 120)
                )
            }
        }
    }


    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pie Chart with center label
        Box(contentAlignment = Alignment.Center) {
            // size chosen to be compact; caller can override via modifier
            val canvasSize = 160.dp
            // captura o color scheme antes de entrarmos no Canvas
            val backgroundCircleColor = MaterialTheme.colorScheme.surfaceVariant

            Canvas(modifier = Modifier.size(canvasSize)) {
                val diameter = size.minDimension
                // stroke thickness relative to diameter (keeps proportions across sizes)
                val strokeWidth = diameter * 0.14f // ~14% of diameter
                val radius = diameter / 2f

                // draw subtle background ring (gives "hole" contrast)
                drawCircle(
                    color = backgroundCircleColor,
                    radius = radius - strokeWidth / 2f
                )

                var startAngle = -90f
                animatedProgress.forEachIndexed { index, animatable ->
                    drawArc(
                        color = data[index].color,
                        startAngle = startAngle,
                        sweepAngle = animatable.value,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += animatable.value
                }
            }

            // Center label: total + unit
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val df = DecimalFormat("#.##")
                Text(
                    text = df.format(totalValue),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = totalUnit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Legend with absolute values + percent
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val df = DecimalFormat("#.##")
            data.forEach { slice ->
                val percent = if (total > 0) 100f * slice.value / total else 0f
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color = slice.color, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${slice.label}: ${df.format(slice.value)} (${df.format(percent)}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
