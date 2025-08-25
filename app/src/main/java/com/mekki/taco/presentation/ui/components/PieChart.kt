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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val totalMacros = data.sumOf { it.value.toDouble() }.toFloat()
    if (totalMacros <= 0f) return // nothing to draw

    val angles = data.map { 360f * it.value / totalMacros }
    val animatedProgress = remember(data) { data.map { Animatable(0f) } }

    LaunchedEffect(data) {
        animatedProgress.forEachIndexed { index, animatable ->
            launch {
                animatable.animateTo(
                    targetValue = angles.getOrElse(index) { 0f },
                    animationSpec = tween(durationMillis = 700, delayMillis = index * 120)
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
            val canvasSize = 160.dp
            val backgroundCircleColor = MaterialTheme.colorScheme.surfaceVariant

            Canvas(modifier = Modifier.size(canvasSize)) {
                val strokeWidth = size.minDimension * 0.15f
                drawCircle(
                    color = backgroundCircleColor,
                    style = Stroke(width = strokeWidth)
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

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val df = DecimalFormat("#")
                Text(
                    text = df.format(totalValue),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = totalUnit,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val dfValue = DecimalFormat("#.#")
            val dfPercent = DecimalFormat("#")

            data.forEach { slice ->
                val percent = if (totalMacros > 0) 100f * slice.value / totalMacros else 0f
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    // Ponto colorido
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color = slice.color, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Nome do Macro
                    Text(
                        text = slice.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    // Espaçador para empurrar o valor para a direita
                    Spacer(modifier = Modifier.weight(1f))
                    // Valor em gramas e porcentagem
                    Text(
                        text = "${dfValue.format(slice.value)}g (${dfPercent.format(percent)}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}
