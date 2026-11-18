package com.pulsehealth.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pulsehealth.app.ui.theme.AthleticCyan

/**
 * Modern Dot Matrix / Circle Grid Chart representing daily volume, consistency,
 * and exercise load as clean vertical bubble stacks (as seen in Image 2 "Training").
 */
@Composable
fun DotMatrixConsistencyChart(
    dailyLevels: List<Int>, // Number of active dots per day (e.g. 0 to 5)
    modifier: Modifier = Modifier,
    maxDotsPerColumn: Int = 5,
    dotColor: Color = AthleticCyan,
    emptyDotColor: Color = Color(0xFFE5EDF0)
) {
    val levels = if (dailyLevels.isNotEmpty()) dailyLevels else listOf(1, 2, 1, 3, 2, 4, 5)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val numCols = levels.size
            val colStep = width / numCols
            val dotRadius = 4.2.dp.toPx()
            val dotGapY = 3.5.dp.toPx()
            val totalDotSlot = (dotRadius * 2) + dotGapY

            levels.forEachIndexed { colIndex, level ->
                val centerX = colIndex * colStep + (colStep / 2)

                for (dotIndex in 0 until maxDotsPerColumn) {
                    // Start from bottom upwards
                    val centerY = height - (dotIndex * totalDotSlot) - dotRadius - 4.dp.toPx()
                    val isActive = dotIndex < level

                    // Calculate subtle progressive alpha for active dots
                    val activeColor = if (isActive) {
                        dotColor.copy(alpha = 0.45f + ((dotIndex + 1).toFloat() / maxDotsPerColumn) * 0.55f)
                    } else {
                        emptyDotColor
                    }

                    drawCircle(
                        color = activeColor,
                        radius = dotRadius,
                        center = Offset(centerX, centerY)
                    )
                }
            }
        }
    }
}
