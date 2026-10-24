package com.example.ui.flight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.ExplosionEffect
import com.example.model.FlakBullet
import com.example.model.MultiplayerPilot
import com.example.model.SamMissile
import com.example.model.TargetEntity
import com.example.model.TargetType
import com.example.physics.FlightPhysics
import com.example.physics.Vector3D
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-performance 3D Canvas rendering engine for FPV Kamikaze Drone simulation.
 * Projects world 3D objects (Pantsir-S1, Tanks, Bunkers, Missiles, Flak, Explosions)
 * onto camera space based on drone 6-DOF position, pitch, roll, and yaw.
 */
@Composable
fun FpvCanvasView(
    physics: FlightPhysics,
    targets: List<TargetEntity>,
    samMissiles: List<SamMissile>,
    flakBullets: List<FlakBullet>,
    explosions: List<ExplosionEffect>,
    competitors: List<MultiplayerPilot>,
    isThermalMode: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f
        val fov = width * 0.85f // Focal length for wide FPV FOV (~115°)

        val camPos = physics.position
        val pitch = physics.pitch + (physics.equipment.cameraAngleDeg * (PI.toFloat() / 180f))
        val roll = physics.roll
        val yaw = physics.yaw

        // 1. Sky & Ground Horizon
        drawAtmosphereAndGround(width, height, centerX, centerY, pitch, roll, isThermalMode)

        // Helper 3D to 2D projection function
        fun project3D(worldPos: Vector3D): Offset? {
            val camSpace = worldPos.transformToCameraSpace(camPos, pitch, roll, yaw)
            if (camSpace.z <= 0.8f) return null // behind camera / too close

            val sx = centerX + (camSpace.x / camSpace.z) * fov
            val sy = centerY - (camSpace.y / camSpace.z) * fov
            return Offset(sx, sy)
        }

        // 2. 3D Terrain Grid & Reference Lines
        drawTerrainGrid(camPos, pitch, roll, yaw, centerX, centerY, fov, isThermalMode, ::project3D)

        // 3. Targets (Pantsir, Tanks, Bunkers, Ammo Depots)
        for (target in targets) {
            drawTarget3D(target, camPos, pitch, roll, yaw, centerX, centerY, fov, isThermalMode, ::project3D)
        }

        // 4. Competitors / Fellow FPV Drones in Multiplayer
        for (drone in competitors) {
            if (!drone.isPlayer && drone.isAlive) {
                drawCompetitorDrone(drone, isThermalMode, ::project3D)
            }
        }

        // 5. SAM Missiles and Smoke Trails
        for (missile in samMissiles) {
            drawSamMissile(missile, isThermalMode, ::project3D)
        }

        // 6. 30mm Flak Cannon Tracers
        for (flak in flakBullets) {
            drawFlakBullet(flak, isThermalMode, ::project3D)
        }

        // 7. Explosions & Volumetric Shockwaves
        for (exp in explosions) {
            drawExplosion(exp, isThermalMode, ::project3D)
        }

        // 8. Electronic Warfare (РЭБ) Analog Glitch & Noise Overlay
        if (physics.ewInterferenceLevel > 0.05f) {
            drawEwInterference(width, height, physics.ewInterferenceLevel)
        }
    }
}

private fun DrawScope.drawAtmosphereAndGround(
    width: Float,
    height: Float,
    centerX: Float,
    centerY: Float,
    pitch: Float,
    roll: Float,
    isThermal: Boolean
) {
    if (isThermal) {
        // FLIR Thermal: Inverted monochrome cool background
        drawRect(Color(0xFF0F1412), Offset.Zero, Size(width, height))
        drawRect(Color(0xFF060907), Offset(0f, centerY + pitch * 300f), Size(width, height))
        return
    }

    // Sky gradient
    val skyColor = Color(0xFF1E2D3D)
    val horizonSky = Color(0xFF5A4433)
    val groundColor = Color(0xFF282F24)
    val groundFar = Color(0xFF3B4032)

    // Base background
    drawRect(skyColor, Offset.Zero, Size(width, height))

    // Ground plane horizon tilt
    val horizonY = centerY + pitch * 320f
    val horizonPath = Path().apply {
        moveTo(0f, horizonY - 40f)
        lineTo(width, horizonY - 40f)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(horizonPath, groundColor)

    // Horizon haze line
    drawLine(
        color = horizonSky,
        start = Offset(0f, horizonY),
        end = Offset(width, horizonY),
        strokeWidth = 4f
    )
}

private fun DrawScope.drawTerrainGrid(
    camPos: Vector3D,
    pitch: Float,
    roll: Float,
    yaw: Float,
    centerX: Float,
    centerY: Float,
    fov: Float,
    isThermal: Boolean,
    project: (Vector3D) -> Offset?
) {
    val gridColor = if (isThermal) Color(0xFF1B2A1E) else Color(0x334B5942)
    val step = 40f
    val range = 480f

    val startX = ((camPos.x / step).toInt() * step) - range
    val endX = startX + range * 2f
    val startZ = ((camPos.z / step).toInt() * step) - 40f
    val endZ = startZ + range

    var z = startZ
    while (z <= endZ) {
        val p1 = project(Vector3D(startX, 0f, z))
        val p2 = project(Vector3D(endX, 0f, z))
        if (p1 != null && p2 != null) {
            drawLine(gridColor, p1, p2, strokeWidth = 1.5f)
        }
        z += step
    }

    var x = startX
    while (x <= endX) {
        val p1 = project(Vector3D(x, 0f, startZ))
        val p2 = project(Vector3D(x, 0f, endZ))
        if (p1 != null && p2 != null) {
            drawLine(gridColor, p1, p2, strokeWidth = 1.5f)
        }
        x += step
    }
}

private fun DrawScope.drawTarget3D(
    target: TargetEntity,
    camPos: Vector3D,
    pitch: Float,
    roll: Float,
    yaw: Float,
    centerX: Float,
    centerY: Float,
    fov: Float,
    isThermal: Boolean,
    project: (Vector3D) -> Offset?
) {
    val basePos = target.position
    val pCenter = project(basePos) ?: return
    val dist = camPos.distanceTo(basePos)
    if (dist > 900f) return

    val scale = (fov / dist.coerceAtLeast(1f)).coerceIn(1.5f, 90f)

    when (target.type) {
        TargetType.PANTSIR_S1 -> {
            drawPantsirModel(target, pCenter, scale, isThermal)
        }
        TargetType.TANK_T72 -> {
            drawTankModel(target, pCenter, scale, isThermal)
        }
        TargetType.AMMO_DEPOT -> {
            drawAmmoDepotModel(target, pCenter, scale, isThermal)
        }
        TargetType.INFANTRY_BUNKER -> {
            drawBunkerModel(target, pCenter, scale, isThermal)
        }
        TargetType.RADAR_MAST -> {
            drawRadarMastModel(target, pCenter, scale, isThermal)
        }
        TargetType.ENEMY_RECON_DRONE -> {
            drawReconDroneModel(target, pCenter, scale, isThermal)
        }
    }
}

private fun DrawScope.drawPantsirModel(
    target: TargetEntity,
    center: Offset,
    scale: Float,
    isThermal: Boolean
) {
    val isDestroyed = target.isDestroyed
    val hullColor = when {
        isDestroyed -> Color(0xFF1A1A1A)
        isThermal -> Color(0xFFFF9E00) // Hot engine and radar thermal signature!
        else -> Color(0xFF3D4B39) // Military Camo Green
    }
    val detailColor = when {
        isDestroyed -> Color(0xFF333333)
        isThermal -> Color(0xFFFFFFFF)
        else -> Color(0xFF222B1E)
    }
    val missilePodColor = if (isThermal) Color(0xFFFF6D00) else Color(0xFF556050)

    val w = scale * 4.2f
    val h = scale * 2.4f

    // 1. Armored 8x8 Kamaz Truck Chassis
    drawRect(
        color = hullColor,
        topLeft = Offset(center.x - w / 2f, center.y - h),
        size = Size(w, h * 0.65f)
    )

    // Wheels (8 wheels)
    val wheelRadius = scale * 0.45f
    val wheelY = center.y - wheelRadius
    for (i in -3..3 step 2) {
        drawCircle(
            color = Color.Black,
            radius = wheelRadius,
            center = Offset(center.x + (i * w / 8f), wheelY)
        )
    }

    // 2. Combat Tower Module (Turret)
    val turretW = scale * 2.2f
    val turretH = scale * 1.5f
    val turretTop = center.y - h - turretH * 0.6f
    drawRect(
        color = detailColor,
        topLeft = Offset(center.x - turretW / 2f, turretTop),
        size = Size(turretW, turretH)
    )

    // 3. Dual Twin 30mm 2A38M Autocannons (Left & Right)
    val gunLength = scale * 2.5f
    val gunElevation = -0.3f
    val gx = cos(gunElevation) * gunLength
    val gy = sin(gunElevation) * gunLength
    // Left guns
    drawLine(
        color = Color(0xFF111111),
        start = Offset(center.x - turretW * 0.55f, turretTop + turretH * 0.4f),
        end = Offset(center.x - turretW * 0.55f - gx, turretTop + turretH * 0.4f + gy),
        strokeWidth = scale * 0.25f
    )
    // Right guns
    drawLine(
        color = Color(0xFF111111),
        start = Offset(center.x + turretW * 0.55f, turretTop + turretH * 0.4f),
        end = Offset(center.x + turretW * 0.55f + gx, turretTop + turretH * 0.4f + gy),
        strokeWidth = scale * 0.25f
    )

    // 4. SAM Missile Launcher Canisters (12 missile tubes in 2 blocks)
    val podW = scale * 0.9f
    val podH = scale * 1.2f
    drawRect(missilePodColor, Offset(center.x - turretW * 0.85f, turretTop - podH * 0.2f), Size(podW, podH))
    drawRect(missilePodColor, Offset(center.x + turretW * 0.85f - podW, turretTop - podH * 0.2f), Size(podW, podH))

    // 5. Rotating Radar Dish on Mast (360° rotation)
    val mastTop = turretTop - scale * 1.4f
    drawLine(Color.DarkGray, Offset(center.x, turretTop), Offset(center.x, mastTop), strokeWidth = scale * 0.2f)

    if (!isDestroyed) {
        val radarRot = target.radarAngle
        val radarW = scale * 2.0f * cos(radarRot)
        val radarH = scale * 0.8f
        val radarColor = if (isThermal) Color(0xFFFFD600) else Color(0xFF8D99AE)

        drawOval(
            color = radarColor,
            topLeft = Offset(center.x - kotlin.math.abs(radarW) / 2f, mastTop - radarH / 2f),
            size = Size(kotlin.math.abs(radarW).coerceAtLeast(scale * 0.3f), radarH)
        )
    }

    // Smoke from destroyed vehicle
    if (isDestroyed) {
        drawCircle(Color(0x99111111), scale * 2.5f, Offset(center.x, center.y - h * 1.5f))
    }
}

private fun DrawScope.drawTankModel(
    target: TargetEntity,
    center: Offset,
    scale: Float,
    isThermal: Boolean
) {
    val isDestroyed = target.isDestroyed
    val hullColor = when {
        isDestroyed -> Color(0xFF1E1E1E)
        isThermal -> Color(0xFFFF8800)
        else -> Color(0xFF424B35)
    }
    val w = scale * 3.8f
    val h = scale * 1.6f

    // Tracks
    drawRoundRect(
        color = Color(0xFF1C1D1B),
        topLeft = Offset(center.x - w / 2f, center.y - h * 0.6f),
        size = Size(w, h * 0.6f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(scale * 0.3f, scale * 0.3f)
    )

    // Hull
    drawRect(hullColor, Offset(center.x - w * 0.45f, center.y - h * 0.9f), Size(w * 0.9f, h * 0.5f))

    // Turret with reactive armor bricks
    val turretR = scale * 1.1f
    val turretCenter = Offset(center.x, center.y - h * 0.9f)
    drawCircle(if (isThermal) Color(0xFFFFC107) else Color(0xFF2C3520), turretR, turretCenter)

    // Main 125mm Gun
    val gunLen = scale * 3.2f
    drawLine(
        color = Color(0xFF181C14),
        start = turretCenter,
        end = Offset(turretCenter.x - gunLen, turretCenter.y - scale * 0.1f),
        strokeWidth = scale * 0.22f
    )
}

private fun DrawScope.drawAmmoDepotModel(
    target: TargetEntity,
    center: Offset,
    scale: Float,
    isThermal: Boolean
) {
    val isDestroyed = target.isDestroyed
    val w = scale * 5.0f
    val h = scale * 2.2f
    val color = if (isDestroyed) Color(0xFF1A1A1A) else if (isThermal) Color(0xFFFF3D00) else Color(0xFF6B583E)

    // Depot crates and camouflage tent
    val tent = Path().apply {
        moveTo(center.x - w / 2f, center.y)
        lineTo(center.x, center.y - h)
        lineTo(center.x + w / 2f, center.y)
        close()
    }
    drawPath(tent, color)

    // Ammunition boxes
    if (!isDestroyed) {
        drawRect(Color(0xFF2F4F2F), Offset(center.x - w * 0.3f, center.y - h * 0.4f), Size(scale * 1.2f, scale * 0.8f))
        drawRect(Color(0xFF8B0000), Offset(center.x + w * 0.1f, center.y - h * 0.4f), Size(scale * 1.2f, scale * 0.8f))
    }
}

private fun DrawScope.drawBunkerModel(
    target: TargetEntity,
    center: Offset,
    scale: Float,
    isThermal: Boolean
) {
    val w = scale * 4.0f
    val h = scale * 1.8f
    val color = if (target.isDestroyed) Color(0xFF141414) else if (isThermal) Color(0xFF7CB342) else Color(0xFF5A574F)

    // Concrete dugout mound
    drawOval(color, Offset(center.x - w / 2f, center.y - h), Size(w, h))
    // Gun embrasure / slit
    drawRect(Color.Black, Offset(center.x - scale * 0.8f, center.y - h * 0.5f), Size(scale * 1.6f, scale * 0.35f))
}

private fun DrawScope.drawRadarMastModel(
    target: TargetEntity,
    center: Offset,
    scale: Float,
    isThermal: Boolean
) {
    val h = scale * 6.0f
    val color = if (target.isDestroyed) Color(0xFF222222) else if (isThermal) Color(0xFF00E5FF) else Color(0xFF8D99AE)

    // Lattice tower
    drawLine(color, center, Offset(center.x, center.y - h), strokeWidth = scale * 0.3f)
    // Guy wires
    drawLine(Color.Gray, Offset(center.x, center.y - h * 0.8f), Offset(center.x - scale * 2f, center.y), strokeWidth = 1f)
    drawLine(Color.Gray, Offset(center.x, center.y - h * 0.8f), Offset(center.x + scale * 2f, center.y), strokeWidth = 1f)

    if (!target.isDestroyed) {
        // Rotating parabolic dish
        drawCircle(Color(0xFFFF5722), scale * 1.2f, Offset(center.x, center.y - h))
    }
}

private fun DrawScope.drawReconDroneModel(
    target: TargetEntity,
    center: Offset,
    scale: Float,
    isThermal: Boolean
) {
    val droneR = scale * 1.2f
    val color = if (isThermal) Color(0xFFFF1744) else Color(0xFFE0E0E0)

    // Quad frame
    drawCircle(color, droneR * 0.4f, center)
    drawLine(Color.Black, Offset(center.x - droneR, center.y - droneR), Offset(center.x + droneR, center.y + droneR), strokeWidth = 2f)
    drawLine(Color.Black, Offset(center.x - droneR, center.y + droneR), Offset(center.x + droneR, center.y - droneR), strokeWidth = 2f)
    // Spinning props
    drawCircle(Color(0x66FFFFFF), droneR * 0.3f, Offset(center.x - droneR, center.y - droneR))
    drawCircle(Color(0x66FFFFFF), droneR * 0.3f, Offset(center.x + droneR, center.y - droneR))
    drawCircle(Color(0x66FFFFFF), droneR * 0.3f, Offset(center.x - droneR, center.y + droneR))
    drawCircle(Color(0x66FFFFFF), droneR * 0.3f, Offset(center.x + droneR, center.y + droneR))
}

private fun DrawScope.drawCompetitorDrone(
    pilot: MultiplayerPilot,
    isThermal: Boolean,
    project: (Vector3D) -> Offset?
) {
    val p = project(pilot.position) ?: return
    val color = if (isThermal) Color(0xFF00E5FF) else Color(0xFF00E676)

    // Small friendly drone indicator
    drawCircle(color, 6f, p)
    drawCircle(Color.White, 3f, p)
}

private fun DrawScope.drawSamMissile(
    missile: SamMissile,
    isThermal: Boolean,
    project: (Vector3D) -> Offset?
) {
    val head = project(missile.position) ?: return

    // Draw smoke particle trail
    for (i in 0 until missile.smokeTrail.size) {
        val sp = project(missile.smokeTrail[i]) ?: continue
        val alpha = (i.toFloat() / missile.smokeTrail.size) * 0.6f
        val r = 4f + (missile.smokeTrail.size - i) * 0.6f
        drawCircle(Color(1f, 1f, 1f, alpha), r, sp)
    }

    // Missile body
    val missileColor = if (isThermal) Color(0xFFFF1744) else Color(0xFFFFD600)
    drawCircle(missileColor, 7f, head)
    drawCircle(Color.White, 3.5f, head)
}

private fun DrawScope.drawFlakBullet(
    flak: FlakBullet,
    isThermal: Boolean,
    project: (Vector3D) -> Offset?
) {
    val p = project(flak.position) ?: return
    val color = if (isThermal) Color(0xFFFF5252) else Color(0xFFFF7043)
    drawCircle(color, 4.5f, p)
}

private fun DrawScope.drawExplosion(
    exp: ExplosionEffect,
    isThermal: Boolean,
    project: (Vector3D) -> Offset?
) {
    val p = project(exp.position) ?: return

    val blastColor = when {
        isThermal -> Color(1f, 1f, 1f, exp.life)
        exp.isCatastrophic -> Color(1f, 0.4f, 0f, exp.life)
        else -> Color(1f, 0.6f, 0.1f, exp.life)
    }

    val r = exp.radius * 6f
    // Fireball
    drawCircle(blastColor, r, p)
    // Shockwave ring
    drawCircle(
        color = Color(1f, 0.9f, 0.3f, exp.life * 0.8f),
        radius = r * 1.4f,
        center = p,
        style = Stroke(width = 4f)
    )
}

private fun DrawScope.drawEwInterference(width: Float, height: Float, intensity: Float) {
    // Analog static / grain noise overlay
    val numLines = (intensity * 40).toInt()
    for (i in 0 until numLines) {
        val y = Random.nextFloat() * height
        val h = Random.nextFloat() * 4f + 1f
        val alpha = (Random.nextFloat() * 0.5f + 0.2f) * intensity
        drawRect(
            color = Color(1f, 1f, 1f, alpha),
            topLeft = Offset(0f, y),
            size = Size(width, h)
        )
    }
}
