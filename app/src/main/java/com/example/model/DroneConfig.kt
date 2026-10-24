package com.example.model

enum class DroneFrameId(
    val title: String,
    val description: String,
    val massKg: Float,
    val maxThrustN: Float,
    val dragCoefficient: Float,
    val priceWP: Int
) {
    SHMEL_7(
        title = "Шмель-7 (7″)",
        description = "Универсальный и маневренный ударный квадрокоптер. Отличный баланс скорости и управляемости.",
        massKg = 1.4f,
        maxThrustN = 44.0f,
        dragCoefficient = 0.28f,
        priceWP = 0
    ),
    MAMONT_10(
        title = "Мамонт-10 (10″)",
        description = "Тяжелый ударный дрон для переноса тяжелых термобарических и тандемных боевых частей.",
        massKg = 2.4f,
        maxThrustN = 62.0f,
        dragCoefficient = 0.38f,
        priceWP = 4500
    ),
    STRELA_X(
        title = "Стрела-Х (Racer)",
        description = "Высокоскоростной перехватчик (до 170 км/ч). Мгновенный разгон и резкий дайв на цель.",
        massKg = 1.1f,
        maxThrustN = 48.0f,
        dragCoefficient = 0.22f,
        priceWP = 8000
    ),
    BUMERANG(
        title = "Бумеранг (Крыло)",
        description = "Дрон-камикадзе самолетного типа. Высокая дальность планирования и стабильность в полете.",
        massKg = 2.0f,
        maxThrustN = 40.0f,
        dragCoefficient = 0.16f,
        priceWP = 12000
    )
}

enum class WarheadType(
    val title: String,
    val spec: String,
    val penetrationMm: Int,
    val blastRadiusM: Float,
    val bonusDamageAgainst: String,
    val massKg: Float,
    val priceWP: Int
) {
    PG_7VL(
        title = "ПГ-7ВЛ (Кумулятив)",
        spec = "500 мм пробития",
        penetrationMm = 500,
        blastRadiusM = 4.5f,
        bonusDamageAgainst = "Бронетехника и моторные отсеки танков",
        massKg = 1.6f,
        priceWP = 0
    ),
    VOG_17(
        title = "ВОГ-17 (Осколочный)",
        spec = "350+ осколков",
        penetrationMm = 120,
        blastRadiusM = 12.0f,
        bonusDamageAgainst = "РЛС Панциря, антенны, пехотные позиции",
        massKg = 0.9f,
        priceWP = 2500
    ),
    TBG_7V(
        title = "ТБГ-7В (Термобар)",
        spec = "Объемный взрыв",
        penetrationMm = 200,
        blastRadiusM = 16.0f,
        bonusDamageAgainst = "Укрытия, блиндажи, склады боеприпасов",
        massKg = 2.2f,
        priceWP = 6000
    ),
    TANDEM_PG(
        title = "Тандем-ПГ (Двойной)",
        spec = "Преодоление ДЗ",
        penetrationMm = 750,
        blastRadiusM = 6.0f,
        bonusDamageAgainst = "Танки с динамической защитой Контакт-5",
        massKg = 2.6f,
        priceWP = 10000
    )
}

enum class MotorTypeId(
    val title: String,
    val kv: Int,
    val thrustMultiplier: Float,
    val efficiency: Float,
    val priceWP: Int
) {
    MOTOR_2807(
        title = "2807 1300KV",
        kv = 1300,
        thrustMultiplier = 1.0f,
        efficiency = 1.0f,
        priceWP = 0
    ),
    MOTOR_3115(
        title = "3115 900KV Heavy",
        kv = 900,
        thrustMultiplier = 1.25f,
        efficiency = 1.15f,
        priceWP = 3500
    ),
    MOTOR_2207(
        title = "2207 2450KV Turbo",
        kv = 2450,
        thrustMultiplier = 1.35f,
        efficiency = 0.85f,
        priceWP = 5500
    )
}

enum class BatteryTypeId(
    val title: String,
    val capacityMah: Int,
    val voltageNominal: Float,
    val cRate: Int,
    val massKg: Float,
    val priceWP: Int
) {
    LIPO_6S_4000(
        title = "LiPo 6S 4000mAh",
        capacityMah = 4000,
        voltageNominal = 22.2f,
        cRate = 60,
        massKg = 0.72f,
        priceWP = 0
    ),
    LIION_6S2P_8000(
        title = "Li-Ion 6S2P 8000mAh",
        capacityMah = 8000,
        voltageNominal = 21.6f,
        cRate = 35,
        massKg = 1.15f,
        priceWP = 4000
    ),
    GRAPHENE_6S_5500(
        title = "Graphene 6S 5500mAh",
        capacityMah = 5500,
        voltageNominal = 22.8f,
        cRate = 120,
        massKg = 0.88f,
        priceWP = 7500
    )
}

enum class SkinId(
    val title: String,
    val hexColorPrimary: Long,
    val hexColorAccent: Long,
    val priceWP: Int,
    val previewBadge: String
) {
    SKIN_PIXEL("Пиксель ВС", 0xFF3F4F3C, 0xFF65785C, 0, "Военный камуфляж"),
    SKIN_DESERT("Пустынный Шквал", 0xFFB3925F, 0xFFE0C58C, 2000, "Песчаный"),
    SKIN_STEALTH("Ночной Охотник", 0xFF14171A, 0xFF2A2E33, 4000, "Матовый черный"),
    SKIN_NEON("Киберпанк 2077", 0xFF0D1B2A, 0xFF00F0FF, 6500, "Неоновые контуры"),
    SKIN_CARBON("Карбон Монокок", 0xFF1E2124, 0xFFFF7A00, 9000, "3K Углеволокно"),
    SKIN_DRAGON("Пламя Дракона", 0xFF6A040F, 0xFFFFBA08, 14000, "Огненная ливрея")
}

data class PlayerDroneEquipment(
    val frame: DroneFrameId = DroneFrameId.SHMEL_7,
    val warhead: WarheadType = WarheadType.PG_7VL,
    val motor: MotorTypeId = MotorTypeId.MOTOR_2807,
    val battery: BatteryTypeId = BatteryTypeId.LIPO_6S_4000,
    val skin: SkinId = SkinId.SKIN_PIXEL,
    val cameraAngleDeg: Float = 25f, // Camera uptilt 0° - 45°
    val isAcroMode: Boolean = false, // false = ANGLE (self-leveling), true = ACRO (manual rate mode)
    val isThermalCamera: Boolean = false
)
