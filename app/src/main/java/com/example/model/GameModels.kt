package com.example.model

import com.example.physics.Vector3D

enum class TargetType(
    val title: String,
    val maxHp: Float,
    val baseRewardWP: Int,
    val hasAirDefense: Boolean,
    val hasEwJammer: Boolean,
    val ewRadiusM: Float
) {
    PANTSIR_S1(
        title = "ЗРПК «Панцирь-С1»",
        maxHp = 250f,
        baseRewardWP = 1500,
        hasAirDefense = true,
        hasEwJammer = true,
        ewRadiusM = 140f
    ),
    TANK_T72(
        title = "Танк Т-72Б3 (с ДЗ)",
        maxHp = 300f,
        baseRewardWP = 1100,
        hasAirDefense = false,
        hasEwJammer = true,
        ewRadiusM = 60f
    ),
    AMMO_DEPOT(
        title = "Склад БК (Полевой)",
        maxHp = 180f,
        baseRewardWP = 1800,
        hasAirDefense = false,
        hasEwJammer = false,
        ewRadiusM = 0f
    ),
    INFANTRY_BUNKER(
        title = "Укрепленный Блиндаж",
        maxHp = 200f,
        baseRewardWP = 800,
        hasAirDefense = false,
        hasEwJammer = false,
        ewRadiusM = 0f
    ),
    RADAR_MAST(
        title = "Радарная Станция РЛС",
        maxHp = 150f,
        baseRewardWP = 1300,
        hasAirDefense = false,
        hasEwJammer = true,
        ewRadiusM = 180f
    ),
    ENEMY_RECON_DRONE(
        title = "Вражеский Дрон-разведчик",
        maxHp = 60f,
        baseRewardWP = 950,
        hasAirDefense = false,
        hasEwJammer = false,
        ewRadiusM = 0f
    )
}

enum class HitZone(val multiplier: Float, val title: String) {
    RADAR_DISH(2.5f, "РЛС УНИЧТОЖЕНА!"),
    AMMO_RACK(3.0f, "ДЕТОНАЦИЯ БОЕКОМПЛЕКТА!"),
    ENGINE_DECK(2.2f, "ПРОБИТИЕ ДВИГАТЕЛЯ!"),
    TURRET(1.4f, "ПОРАЖЕНИЕ БАШНИ"),
    HULL(1.0f, "ПОПАДАНИЕ В КОРПУС"),
    REACTIVE_ARMOR(0.35f, "ДИНАМИЧЕСКАЯ ЗАЩИТА СРАБОТАЛА")
}

data class TargetEntity(
    val id: String,
    val type: TargetType,
    var position: Vector3D,
    var rotationY: Float = 0f,
    var hp: Float = type.maxHp,
    var radarAngle: Float = 0f,
    var isDestroyed: Boolean = false,
    var lastShotTimeMs: Long = 0L,
    var lastMissileTimeMs: Long = 0L,
    var destroyedParts: MutableSet<String> = mutableSetOf()
)

data class SamMissile(
    val id: String,
    var position: Vector3D,
    var velocity: Vector3D,
    var lifeSec: Float = 6.0f,
    val smokeTrail: MutableList<Vector3D> = mutableListOf()
)

data class FlakBullet(
    var position: Vector3D,
    var velocity: Vector3D,
    var lifeSec: Float = 2.5f
)

data class ExplosionEffect(
    val position: Vector3D,
    var radius: Float = 1.0f,
    val maxRadius: Float = 12.0f,
    var life: Float = 1.0f, // 1.0 down to 0.0
    val isCatastrophic: Boolean = false
)

data class MultiplayerPilot(
    val callsign: String,
    val isPlayer: Boolean,
    var position: Vector3D,
    var kills: Int,
    var score: Int,
    var isAlive: Boolean = true,
    var pingMs: Int = 24
)
