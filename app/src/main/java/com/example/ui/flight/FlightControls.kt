package com.example.ui.flight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TacticalOrange
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun FlightControls(
    throttle: Float,
    isAcro: Boolean,
    isThermal: Boolean,
    isArmed: Boolean,
    onThrottleChange: (Float) -> Unit,
    onYawChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onRollChange: (Float) -> Unit,
    onToggleAcro: () -> Unit,
    onToggleThermal: () -> Unit,
    onToggleArm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Quick Actions Bar Top Center
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // FLIR Thermal button
            FilledTonalButton(
                onClick = onToggleThermal,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isThermal) Color(0xFF00E676) else Color(0x66000000),
                    contentColor = if (isThermal) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("flir_thermal_button")
            ) {
                Text(
                    text = if (isThermal) "FLIR: ВКЛ" else "ТЕПЛОВИЗОР",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ACRO / ANGLE toggle button
            FilledTonalButton(
                onClick = onToggleAcro,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isAcro) TacticalOrange else Color(0x66000000),
                    contentColor = if (isAcro) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("acro_mode_button")
            ) {
                Text(
                    text = if (isAcro) "РЕЖИМ: ACRO" else "РЕЖИМ: ANGLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ARM / DISARM
            Button(
                onClick = onToggleArm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isArmed) Color(0x99D50000) else Color(0xFF00C853),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("arm_disarm_button")
            ) {
                Text(
                    text = if (isArmed) "DISARM" else "ARM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Left Virtual Stick (Throttle + Yaw) - Mode 2 FPV Radio
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 18.dp)
        ) {
            VirtualStick(
                initialX = 0f,
                initialY = (throttle * 2f - 1f), // -1 to 1 mapping
                autoCenterHorizontal = true,
                autoCenterVertical = false,
                onStickMove = { x, y ->
                    onYawChange(x)
                    // y is -1 (bottom) to +1 (top) -> map to 0.0 to 1.0
                    val thr = ((y + 1f) / 2f).coerceIn(0f, 1f)
                    onThrottleChange(thr)
                },
                label = "ГАЗ / РЫСКАНИЕ",
                testTag = "throttle_yaw_stick"
            )
        }

        // Right Virtual Stick (Pitch + Roll) - Mode 2 FPV Radio
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 28.dp, bottom = 18.dp)
        ) {
            VirtualStick(
                initialX = 0f,
                initialY = 0f,
                autoCenterHorizontal = true,
                autoCenterVertical = true,
                onStickMove = { x, y ->
                    onRollChange(x)
                    onPitchChange(y)
                },
                label = "ТАНГАЖ / КРЕН",
                testTag = "pitch_roll_stick"
            )
        }
    }
}

@Composable
fun VirtualStick(
    initialX: Float,
    initialY: Float,
    autoCenterHorizontal: Boolean,
    autoCenterVertical: Boolean,
    onStickMove: (Float, Float) -> Unit,
    label: String,
    testTag: String
) {
    val radius = 65f // dp radius
    var knobOffset by remember { mutableStateOf(Offset(initialX * 50f, -initialY * 50f)) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .testTag(testTag)
                .pointerInput(Unit) {
                    val maxRadiusPx = 55.dp.toPx()

                    detectDragGestures(
                        onDragStart = { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val delta = offset - center
                            val dist = delta.getDistance()
                            val clamped = if (dist > maxRadiusPx) delta * (maxRadiusPx / dist) else delta
                            knobOffset = clamped

                            val normX = (clamped.x / maxRadiusPx).coerceIn(-1f, 1f)
                            val normY = (-clamped.y / maxRadiusPx).coerceIn(-1f, 1f)
                            onStickMove(normX, normY)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val next = knobOffset + dragAmount
                            val dist = next.getDistance()
                            val maxRadiusPx = 55.dp.toPx()
                            val clamped = if (dist > maxRadiusPx) next * (maxRadiusPx / dist) else next
                            knobOffset = clamped

                            val normX = (clamped.x / maxRadiusPx).coerceIn(-1f, 1f)
                            val normY = (-clamped.y / maxRadiusPx).coerceIn(-1f, 1f)
                            onStickMove(normX, normY)
                        },
                        onDragEnd = {
                            val maxRadiusPx = 55.dp.toPx()
                            val curX = if (autoCenterHorizontal) 0f else knobOffset.x
                            val curY = if (autoCenterVertical) 0f else knobOffset.y
                            knobOffset = Offset(curX, curY)

                            val normX = (curX / maxRadiusPx).coerceIn(-1f, 1f)
                            val normY = (-curY / maxRadiusPx).coerceIn(-1f, 1f)
                            onStickMove(normX, normY)
                        },
                        onDragCancel = {
                            val maxRadiusPx = 55.dp.toPx()
                            val curX = if (autoCenterHorizontal) 0f else knobOffset.x
                            val curY = if (autoCenterVertical) 0f else knobOffset.y
                            knobOffset = Offset(curX, curY)

                            val normX = (curX / maxRadiusPx).coerceIn(-1f, 1f)
                            val normY = (-curY / maxRadiusPx).coerceIn(-1f, 1f)
                            onStickMove(normX, normY)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.width / 2f - 4f

                // Outer Gimbal Ring
                drawCircle(
                    color = Color(0x33000000),
                    radius = baseRadius,
                    center = center
                )
                drawCircle(
                    color = Color(0x66FFFFFF),
                    radius = baseRadius,
                    center = center,
                    style = Stroke(width = 2f)
                )

                // Gimbal Cross Axes
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(center.x - baseRadius, center.y),
                    end = Offset(center.x + baseRadius, center.y),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(center.x, center.y - baseRadius),
                    end = Offset(center.x, center.y + baseRadius),
                    strokeWidth = 1f
                )

                // Stick Knob
                val knobCenter = center + knobOffset
                drawCircle(
                    color = Color(0xFF1E2836),
                    radius = 24.dp.toPx(),
                    center = knobCenter
                )
                drawCircle(
                    color = TacticalOrange,
                    radius = 24.dp.toPx(),
                    center = knobCenter,
                    style = Stroke(width = 2.5f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = knobCenter
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = Color(0xCCFFFFFF),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
