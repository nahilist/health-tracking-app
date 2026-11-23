package com.pulsehealth.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.pulsehealth.app.ui.theme.AthleticOrange

private val DefaultSparklinePoints = listOf(0.35f, 0.48f, 0.88f, 0.42f, 0.65f, 0.72f, 0.55f)

/**
 * Ultra-modern smooth bezier sparkline curve with subtle dotted grid lines,
 * gradient shadow, and an active orange peak highlight dot (as seen in Granger/modern fitness UI).
 * Optimized with [drawWithCache] to achieve zero allocations and maximum frame rate during scrolling.
 */
@Composable
fun SmoothSparklineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = AthleticOrange,
    gridColor: Color = Color(0xFFE5E7EB),
    highlightIndex: Int = -1
) {
    val points = remember(values) {
        if (values.size >= 2) values else DefaultSparklinePoints
    }
    val peakIdx = remember(highlightIndex, points) {
        if (highlightIndex in points.indices) highlightIndex else points.indices.maxByOrNull { points[it] } ?: (points.size / 2)
    }

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .drawWithCache {
                val width = size.width
                val height = size.height
                val stepX = width / (points.size - 1).coerceAtLeast(1)

                val dottedEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)

                // Compute Bezier Paths only once per size or points change
                val path = Path()
                val fillPath = Path()

                val coords = Array(points.size) { index ->
                    val x = index * stepX
                    val normalizedY = 1.0f - points[index].coerceIn(0.05f, 0.95f)
                    val y = normalizedY * (height * 0.8f) + (height * 0.1f)
                    Offset(x, y)
                }

                path.moveTo(coords[0].x, coords[0].y)
                fillPath.moveTo(coords[0].x, height)
                fillPath.lineTo(coords[0].x, coords[0].y)

                for (i in 0 until coords.size - 1) {
                    val p0 = coords[i]
                    val p1 = coords[i + 1]
                    val controlX = (p0.x + p1.x) / 2f
                    path.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                    fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                }

                fillPath.lineTo(coords.last().x, height)
                fillPath.close()

                val fillBrush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.18f),
                        lineColor.copy(alpha = 0.04f),
                        Color.Transparent
                    )
                )

                val strokeStyle = Stroke(
                    width = 2.8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
                val gridStrokeWidth = 1.dp.toPx()
                val outerGlowRadius = 9.dp.toPx()
                val centerDotRadius = 5.dp.toPx()
                val innerEyeRadius = 2.dp.toPx()
                val peakCoord = coords[peakIdx]
                val peakX = peakIdx * stepX

                onDrawBehind {
                    // Dotted horizontal grid lines
                    val gridRows = 3
                    for (r in 0..gridRows) {
                        val y = (height / gridRows) * r
                        drawLine(
                            color = gridColor.copy(alpha = 0.7f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            pathEffect = dottedEffect,
                            strokeWidth = gridStrokeWidth
                        )
                    }

                    // Vertical peak guide line
                    drawLine(
                        color = gridColor.copy(alpha = 0.9f),
                        start = Offset(peakX, 0f),
                        end = Offset(peakX, height),
                        pathEffect = dottedEffect,
                        strokeWidth = gridStrokeWidth
                    )

                    // Draw soft ambient gradient under curve
                    drawPath(path = fillPath, brush = fillBrush)

                    // Draw main line curve
                    drawPath(path = path, color = lineColor, style = strokeStyle)

                    // Draw glowing peak highlight dot
                    drawCircle(color = lineColor.copy(alpha = 0.25f), radius = outerGlowRadius, center = peakCoord)
                    drawCircle(color = lineColor, radius = centerDotRadius, center = peakCoord)
                    drawCircle(color = Color.White, radius = innerEyeRadius, center = peakCoord)
                }
            }
    )
}
