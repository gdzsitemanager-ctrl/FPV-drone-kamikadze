package com.example.physics

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3D(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vector3D(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float) = if (scalar != 0f) Vector3D(x / scalar, y / scalar, z / scalar) else this

    fun length(): Float = sqrt(x * x + y * y + z * z)
    fun lengthSquared(): Float = x * x + y * y + z * z

    fun normalized(): Vector3D {
        val len = length()
        return if (len > 0.0001f) this / len else Vector3D(0f, 0f, 0f)
    }

    fun distanceTo(other: Vector3D): Float = (this - other).length()

    /**
     * Rotate this world-space vector into drone camera local space
     * given drone pitch (radians), roll (radians), and yaw (radians).
     * Coordinate system:
     * X = Right, Y = Up, Z = Forward
     */
    fun transformToCameraSpace(camPos: Vector3D, pitch: Float, roll: Float, yaw: Float): Vector3D {
        // Translation relative to camera
        val rel = this - camPos

        // 1. Yaw rotation around Y axis
        val cy = cos(-yaw)
        val sy = sin(-yaw)
        val x1 = rel.x * cy - rel.z * sy
        val y1 = rel.y
        val z1 = rel.x * sy + rel.z * cy

        // 2. Pitch rotation around X axis
        val cp = cos(-pitch)
        val sp = sin(-pitch)
        val x2 = x1
        val y2 = y1 * cp - z1 * sp
        val z2 = y1 * sp + z1 * cp

        // 3. Roll rotation around Z axis
        val cr = cos(-roll)
        val sr = sin(-roll)
        val x3 = x2 * cr - y2 * sr
        val y3 = x2 * sr + y2 * cr
        val z3 = z2

        return Vector3D(x3, y3, z3)
    }
}
