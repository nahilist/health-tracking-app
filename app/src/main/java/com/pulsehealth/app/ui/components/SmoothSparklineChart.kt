package com.pulsehealth.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

/**
 * Ultra-modern smooth bezier sparkline curve with subtle dotted grid lines,
 * gradient shadow, and an active orange peak highlight dot (as seen in Granger/modern fitness UI).
 */
@Composable
fun SmoothSparklineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = AthleticOrange,
    gridColor: Color = Color(0xFFE5E7EB),
    highlightIndex: Int = -1
) {
    val points = if (values.size >= 2) values else listOf(0.3f, 0.5f, 0.85f, 0.4f, 0.6f, 0.75f, 0.5f)
    val peakIdx = if (highlightIndex in points.indices) highlightIndex else points.indices.maxByOrNull { points[it] } ?: (points.size / 2)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val stepX = width / (points.size - 1).coerceAtLeast(1)

            // 1. Draw Subtle Dotted Grid Matrix Lines
            val dottedEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
            val gridRows = 3
            for (r in 0..gridRows) {
                val y = (height / gridRows) * r
                drawLine(
                    color = gridColor.copy(alpha = 0.7f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    pathEffect = dottedEffect,
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Vertical peak guide line
            val peakX = peakIdx * stepX
            drawLine(
                color = gridColor.copy(alpha = 0.9f),
                start = Offset(peakX, 0f),
                end = Offset(peakX, height),
                pathEffect = dottedEffect,
                strokeWidth = 1.dp.toPx()
            )

            // 2. Compute Smooth Cubic Bezier Path
            val path = Path()
            val fillPath = Path()

            val coords = points.mapIndexed { index, value ->
                val x = index * stepX
                val normalizedY = 1.0f - value.coerceIn(0.05f, 0.95f)
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

            // 3. Draw Soft Ambient Gradient Under Curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.18f),
                        lineColor.copy(alpha = 0.04f),
                        Color.Transparent
                    )
                )
            )

            // 4. Draw Main Line Path
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 2.8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 5. Draw Glowing Peak Highlight Dot
            val peakCoord = coords[peakIdx]
            // Outer glow ring
            drawCircle(
                color = lineColor.copy(alpha = 0.25f),
                radius = 9.dp.toPx(),
                center = peakCoord
            )
            // Solid center dot
            drawCircle(
                color = lineColor,
                radius = 5.dp.toPx(),
                center = peakCoord
            )
            // White inner eye
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = peakCoord
            )
        }
    }
}
