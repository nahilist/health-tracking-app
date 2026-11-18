package com.pulsehealth.app.ui.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.ui.theme.ForestEmerald
import com.pulsehealth.app.ui.theme.ForestEmeraldDark
import com.pulsehealth.app.ui.theme.ForestEmeraldLight
import com.pulsehealth.app.ui.theme.SoftCreamBackground
import com.pulsehealth.app.ui.theme.WarmAccent
import com.pulsehealth.app.ui.theme.WarmOrange

/**
 * Modern botanical health and vitality header artwork inspired by the executive mobile
 * design system (organic background forms, stylized leaves, citrus vitality accents,
 * heartbeat pulse lines, and brand typography).
 */
@Composable
fun PulseBrandHeaderArt(
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    showTagline: Boolean = true
) {
    val height = if (isCompact) 170.dp else 270.dp

    val transition = rememberInfiniteTransition(label = "PulseHeaderWave")
    val waveOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WavePhase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(SoftCreamBackground),
        contentAlignment = Alignment.Center
    ) {
        // Decorative Vector Canvas with Organic Blobs, Leaves & Pulse Waves
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val h = size.height

            // 1. Soft Warm Pastel Background Curves (Top Corner Elements)
            val topBlobPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(width * 0.45f, 0f)
                cubicTo(
                    width * 0.35f, h * 0.40f,
                    width * 0.15f, h * 0.55f,
                    0f, h * 0.45f
                )
                close()
            }
            drawPath(
                path = topBlobPath,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFEFE7DB), SoftCreamBackground),
                    center = Offset(0f, 0f),
                    radius = width * 0.55f
                )
            )

            // 2. Right Corner Botanical Basket & Leaves Backdrop Form
            val rightBlobPath = Path().apply {
                moveTo(width * 0.55f, 0f)
                lineTo(width, 0f)
                lineTo(width, h * 0.85f)
                cubicTo(
                    width * 0.82f, h * 0.70f + (waveOffset * 10f),
                    width * 0.65f, h * 0.45f,
                    width * 0.55f, 0f
                )
                close()
            }
            drawPath(
                path = rightBlobPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE4ECE8), Color(0xFFF0F5F2)),
                    start = Offset(width, 0f),
                    end = Offset(width * 0.5f, h * 0.6f)
                )
            )

            // 3. Stylized Stylized Health Leaves (Right Upper Canopy)
            val leafColor = ForestEmerald.copy(alpha = 0.22f)
            val leafAccentColor = ForestEmeraldDark.copy(alpha = 0.30f)

            // Leaf 1
            drawOval(
                color = leafColor,
                topLeft = Offset(width * 0.82f, h * 0.10f),
                size = Size(width * 0.18f, h * 0.28f)
            )
            // Leaf 2
            drawOval(
                color = leafAccentColor,
                topLeft = Offset(width * 0.72f, h * 0.18f),
                size = Size(width * 0.16f, h * 0.24f)
            )

            // 4. Warm Citrus / Vitality Fruit Accent Spheres
            drawCircle(
                color = WarmOrange.copy(alpha = 0.85f),
                radius = 18f,
                center = Offset(width * 0.88f, h * 0.38f)
            )
            drawCircle(
                color = WarmAccent.copy(alpha = 0.75f),
                radius = 12f,
                center = Offset(width * 0.80f, h * 0.44f)
            )
            drawCircle(
                color = Color(0xFFFFB74D).copy(alpha = 0.90f),
                radius = 10f,
                center = Offset(width * 0.92f, h * 0.24f)
            )

            // 5. Stylized Pulse ECG Vitality Wave Line (Subtle bottom decorative line)
            val pulsePath = Path().apply {
                val waveY = h * 0.80f
                moveTo(width * 0.05f, waveY)
                lineTo(width * 0.25f, waveY)
                lineTo(width * 0.29f, waveY - 14f)
                lineTo(width * 0.33f, waveY + 20f)
                lineTo(width * 0.38f, waveY - 32f)
                lineTo(width * 0.43f, waveY + 18f)
                lineTo(width * 0.47f, waveY - 8f)
                lineTo(width * 0.51f, waveY)
                lineTo(width * 0.95f, waveY)
            }
            drawPath(
                path = pulsePath,
                color = ForestEmerald.copy(alpha = 0.18f),
                style = Stroke(
                    width = 2.5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Center Brand Identity (Logo + Name + Tagline)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Heartbeat Vitality Emblem
            Surface(
                shape = CircleShape,
                color = ForestEmeraldLight,
                modifier = Modifier.size(if (isCompact) 48.dp else 64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = ForestEmerald,
                        modifier = Modifier.size(if (isCompact) 26.dp else 34.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Brand Typography: Pulse (Forest Emerald) + Health (Warm Orange Accent)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Pulse",
                    fontSize = if (isCompact) 24.sp else 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ForestEmeraldDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Health",
                    fontSize = if (isCompact) 24.sp else 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmAccent,
                    letterSpacing = (-0.5).sp
                )
            }

            if (showTagline && !isCompact) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Private Wellness & Clinical Health Vault",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ForestEmerald.copy(alpha = 0.85f),
                    letterSpacing = 0.4.sp
                )
            }
        }
    }
}
