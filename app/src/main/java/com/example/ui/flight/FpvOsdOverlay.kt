package com.example.ui.flight

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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TargetEntity
import com.example.physics.FlightPhysics
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun FpvOsdOverlay(
    physics: FlightPhysics,
    targets: List<TargetEntity>,
    isThermalMode: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    val osdColor = if (isThermalMode) Color(0xFF00FF66) else Color.White
    val alertColor = Color(0xFFFF1744)

    val nearestTarget = targets.filter { !it.isDestroyed }
        .minByOrNull { it.position.distanceTo(physics.position) }

    val distToNearest = nearestTarget?.position?.distanceTo(physics.position) ?: 999f

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Artificial Horizon Canvas in Center
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            val pitch = physics.pitch
            val roll = physics.roll

            // Artificial Horizon Ladder
            val ladderPitchOffset = pitch * 220f
            val rollCos = cos(-roll)
            val rollSin = sin(-roll)

            // Horizon Center Bar
            val barHalfLen = 45f
            val p1 = Offset(cx - barHalfLen * rollCos, cy + ladderPitchOffset - barHalfLen * rollSin)
            val p2 = Offset(cx + barHalfLen * rollCos, cy + ladderPitchOffset + barHalfLen * rollSin)
            drawLine(osdColor, p1, p2, strokeWidth = 2.5f)

            // Pitch ladder ticks (+10°, -10°)
            for (angleDeg in listOf(15, 30, -15, -30)) {
                val tickPitchOffset = (pitch - (angleDeg * PI.toFloat() / 180f)) * 220f
                val tickLen = 22f
                val tp1 = Offset(cx - tickLen * rollCos, cy + tickPitchOffset - tickLen * rollSin)
                val tp2 = Offset(cx + tickLen * rollCos, cy + tickPitchOffset + tickLen * rollSin)
                drawLine(osdColor.copy(alpha = 0.65f), tp1, tp2, strokeWidth = 1.5f)
            }

            // Central Aim Crosshair
            val crosshairSize = 14f
            drawLine(osdColor, Offset(cx - crosshairSize, cy), Offset(cx - 4f, cy), strokeWidth = 2f)
            drawLine(osdColor, Offset(cx + 4f, cy), Offset(cx + crosshairSize, cy), strokeWidth = 2f)
            drawLine(osdColor, Offset(cx, cy - crosshairSize), Offset(cx, cy - 4f), strokeWidth = 2f)
            drawLine(osdColor, Offset(cx, cy + 4f), Offset(cx, cy + crosshairSize), strokeWidth = 2f)

            // Target Lock Box if near
            if (nearestTarget != null && distToNearest < 600f) {
                val boxSize = (350f / distToNearest.coerceAtLeast(10f)).coerceIn(24f, 85f)
                drawRect(
                    color = if (distToNearest < 180f) alertColor else Color(0xFFFF9E00),
                    topLeft = Offset(cx - boxSize / 2f, cy - boxSize / 2f),
                    size = androidx.compose.ui.geometry.Size(boxSize, boxSize),
                    style = Stroke(width = 2f)
                )
            }
        }

        // 2. Top Bar Telemetry
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Top Left: Battery & Current
            Column {
                val isLowBatt = physics.voltage < 21.4f
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.US, "%.1fV", physics.voltage),
                        color = if (isLowBatt) alertColor.copy(alpha = blinkAlpha) else osdColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${physics.currentDrawA.roundToInt()}A",
                        color = osdColor.copy(alpha = 0.8f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "${physics.mahConsumed.roundToInt()} mAh",
                    color = osdColor.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                if (isLowBatt) {
                    Text(
                        text = "LOW VOLTAGE",
                        color = alertColor.copy(alpha = blinkAlpha),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Top Center: Target Lock Info
            if (nearestTarget != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ЦЕЛЬ: ${nearestTarget.type.title.uppercase()}",
                        color = Color(0xFFFFB300),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "ДИСТАНЦИЯ: ${distToNearest.roundToInt()} М",
                        color = osdColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Top Right: Timer, RSSI & EW Status
            Column(horizontalAlignment = Alignment.End) {
                val minutes = (physics.flightTimerSec / 60).toInt()
                val seconds = (physics.flightTimerSec % 60).toInt()
                Text(
                    text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                    color = osdColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "RSSI: ${physics.rssiPct}%",
                        color = if (physics.rssiPct < 40) alertColor else osdColor.copy(alpha = 0.85f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }

                if (physics.ewInterferenceLevel > 0.15f) {
                    Text(
                        text = "РЭБ АКТИВНО",
                        color = alertColor.copy(alpha = blinkAlpha),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 3. Bottom Bar Telemetry
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Bottom Left: Mode & Throttle
            Column {
                Text(
                    text = if (physics.equipment.isAcroMode) "[ACRO MODE]" else "[ANGLE MODE]",
                    color = if (physics.equipment.isAcroMode) Color(0xFFFF9100) else Color(0xFF00E5FF),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "БЧ: ${physics.equipment.warhead.title}",
                    color = osdColor.copy(alpha = 0.75f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Text(
                    text = "УГОЛ КАМЕРЫ: ${physics.equipment.cameraAngleDeg.toInt()}°",
                    color = osdColor.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.weight(1f))

            // Bottom Center: Arm status
            Text(
                text = "ARMED",
                color = Color(0xFF00E676),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Spacer(Modifier.weight(1f))

            // Bottom Right: Speed & Altitude
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${physics.speedKmh().roundToInt()} КМ/Ч",
                    color = osdColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = String.format(Locale.US, "ВЫСОТА: %.1f М", physics.position.y),
                    color = osdColor.copy(alpha = 0.85f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
                if (isThermalMode) {
                    Text(
                        text = "FLIR THERMAL",
                        color = Color(0xFF39FF14),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
