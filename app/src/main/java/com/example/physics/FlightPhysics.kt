package com.example.physics

import com.example.model.PlayerDroneEquipment
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class FlightPhysics(
    val equipment: PlayerDroneEquipment,
    val thrustMultiplier: Float = 1.0f,
    val batteryEfficiency: Float = 1.0f,
    val ewResistanceMultiplier: Float = 1.0f
) {
    // Spatial state
    var position: Vector3D = Vector3D(0f, 15f, 0f) // start 15m in the air at home base
    var velocity: Vector3D = Vector3D(0f, 0f, 0f)

    // Orientation in radians
    var pitch: Float = 0.15f // slightly pitched forward
    var roll: Float = 0f
    var yaw: Float = 0f

    // Angular rates (rad/s)
    var pitchRate: Float = 0f
    var rollRate: Float = 0f
    var yawRate: Float = 0f

    // Battery simulation
    val maxVoltage: Float = 25.2f
    val minVoltage: Float = 20.8f
    var voltage: Float = 25.2f
    var currentDrawA: Float = 0f
    var mahConsumed: Float = 0f
    var flightTimerSec: Float = 0f

    // Signal & Jamming
    var rssiPct: Int = 99
    var ewInterferenceLevel: Float = 0f // 0.0 = clean, 1.0 = heavy static

    val totalMassKg: Float = equipment.frame.massKg +
            equipment.warhead.massKg +
            equipment.battery.massKg

    val maxThrustN: Float = equipment.frame.maxThrustN *
            equipment.motor.thrustMultiplier *
            thrustMultiplier

    /**
     * Advance simulation by dt seconds given user stick inputs.
     * throttleInput: 0.0 to 1.0
     * pitchInput: -1.0 (dive/forward) to +1.0 (climb/back)
     * rollInput: -1.0 (roll left) to +1.0 (roll right)
     * yawInput: -1.0 (yaw left) to +1.0 (yaw right)
     */
    fun update(
        dt: Float,
        throttleInput: Float,
        pitchInput: Float,
        rollInput: Float,
        yawInput: Float,
        isArmed: Boolean,
        ewJammerDistances: List<Float>
    ) {
        if (!isArmed) {
            // Freefall if disarmed
            velocity = velocity + Vector3D(0f, -9.81f * dt, 0f)
            position = position + velocity * dt
            if (position.y < 0.2f) {
                position = Vector3D(position.x, 0.2f, position.z)
                velocity = Vector3D(0f, 0f, 0f)
            }
            return
        }

        flightTimerSec += dt

        // 1. EW Jamming calculation
        var maxEw = 0f
        for (dist in ewJammerDistances) {
            val effectiveRadius = 135f / ewResistanceMultiplier
            if (dist < effectiveRadius) {
                val factor = (1f - dist / effectiveRadius).coerceIn(0f, 1f)
                // Nap-of-the-earth radar shadow: if flying lower than 5m, terrain shields from EW
                val terrainShield = if (position.y < 6f) 0.5f else 1.0f
                val ew = factor * terrainShield
                if (ew > maxEw) maxEw = ew
            }
        }
        ewInterferenceLevel = maxEw

        // Distance from spawn
        val distFromBase = sqrt(position.x * position.x + position.z * position.z)
        val baseRssi = (100f - (distFromBase / 1500f) * 40f).coerceIn(10f, 99f)
        val jammedRssi = baseRssi * (1.0f - maxEw * 0.75f)
        rssiPct = jammedRssi.toInt()

        // 2. Battery discharge & sag
        val baseAmp = 3.5f // idle electronics + video tx
        val motorAmp = throttleInput * throttleInput * 68.0f
        currentDrawA = baseAmp + motorAmp
        val mahDelta = (currentDrawA * 1000f / 3600f) * dt / batteryEfficiency
        mahConsumed += mahDelta

        val battCapacity = equipment.battery.capacityMah.toFloat()
        val remainingRatio = (1.0f - mahConsumed / battCapacity).coerceIn(0f, 1f)
        val sag = currentDrawA * 0.038f // voltage drop under heavy amp draw
        voltage = (21.0f + remainingRatio * 4.2f - sag).coerceIn(19.5f, 25.2f)

        // 3. Angular control (Acro vs Angle mode)
        val isAcro = equipment.isAcroMode
        if (isAcro) {
            // Direct angular rates
            pitchRate = pitchInput * 4.5f
            rollRate = rollInput * 5.2f
            yawRate = yawInput * 3.8f

            pitch += pitchRate * dt
            roll += rollRate * dt
            yaw += yawRate * dt
        } else {
            // Self-leveling Angle mode (smooth stabilization towards target angle)
            val maxAngleRad = 52f * (PI.toFloat() / 180f)
            val targetPitch = -pitchInput * maxAngleRad // pushing forward sticks tilts nose down
            val targetRoll = rollInput * maxAngleRad

            pitch += (targetPitch - pitch) * 7.5f * dt
            roll += (targetRoll - roll) * 7.5f * dt
            yaw += yawInput * 2.8f * dt
        }

        // Clamp roll/pitch in Angle mode
        if (!isAcro) {
            pitch = pitch.coerceIn(-1.2f, 1.2f)
            roll = roll.coerceIn(-1.2f, 1.2f)
        }

        // 4. Thrust vector computation
        // Drone body normal rotated by pitch, roll, and yaw
        val cp = cos(pitch)
        val sp = sin(pitch)
        val cr = cos(roll)
        val sr = sin(roll)
        val cy = cos(yaw)
        val sy = sin(yaw)

        // Local up vector (0, 1, 0) rotated by roll, pitch, yaw:
        // pitch tilts forward/back along local Z; roll tilts along local X
        val localThrustX = sr * cp
        val localThrustY = cr * cp
        val localThrustZ = -sp

        // Rotate by yaw around world Y
        val thrustX = localThrustX * cy + localThrustZ * sy
        val thrustY = localThrustY
        val thrustZ = -localThrustX * sy + localThrustZ * cy

        val thrustMagnitude = throttleInput * maxThrustN
        val thrustVector = Vector3D(thrustX, thrustY, thrustZ) * thrustMagnitude

        // 5. Gravity & Aerodynamic Drag
        val gravityForce = Vector3D(0f, -9.81f * totalMassKg, 0f)
        val speed = velocity.length()
        val airDensity = 1.225f
        val dragForceMag = 0.5f * airDensity * speed * speed * equipment.frame.dragCoefficient
        val dragVector = if (speed > 0.01f) (velocity.normalized()) * (-dragForceMag) else Vector3D(0f, 0f, 0f)

        // Acceleration = F_total / mass
        val totalForce = thrustVector + gravityForce + dragVector
        val acceleration = totalForce / totalMassKg

        // Integrate velocity & position
        velocity = velocity + acceleration * dt
        position = position + velocity * dt

        // Ground collision
        if (position.y < 0.3f) {
            position = Vector3D(position.x, 0.3f, position.z)
            if (velocity.y < -3.0f) {
                // Hard landing / bounce
                velocity = Vector3D(velocity.x * 0.5f, -velocity.y * 0.2f, velocity.z * 0.5f)
            } else {
                velocity = Vector3D(velocity.x * 0.8f, 0f, velocity.z * 0.8f)
            }
        }
    }

    fun speedKmh(): Float = velocity.length() * 3.6f
}
