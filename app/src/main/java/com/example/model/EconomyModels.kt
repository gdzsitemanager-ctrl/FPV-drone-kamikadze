package com.example.model

enum class ModuleType(
    val title: String,
    val description: String,
    val iconName: String,
    val maxLevel: Int = 5,
    val baseCostWP: Int = 1200
) {
    MOTOR_THRUST(
        title = "Тяга Двигателей",
        description = "Увеличивает максимальную скорость и вертикальный подрыв на +10% за уровень.",
        iconName = "speed",
        baseCostWP = 1000
    ),
    BATTERY_CAPACITY(
        title = "Емкость Батареи",
        description = "Снижает просадку напряжения (sag) и продлевает время полета на +15% за уровень.",
        iconName = "battery",
        baseCostWP = 1200
    ),
    EW_SHIELDING(
        title = "Защита от РЭБ (Помехоустойчивость)",
        description = "Уменьшает дистанцию глушения аналогового видео и сбоев управления Панциря на +18% за уровень.",
        iconName = "shield",
        baseCostWP = 1500
    ),
    WARHEAD_YIELD(
        title = "Мощность Боевой Части",
        description = "Повышает радиус фугасного разлета и бронепробитие кумулятивной струи на +12% за уровень.",
        iconName = "bomb",
        baseCostWP = 1800
    ),
    THERMAL_OPTICS(
        title = "FPV Оптика и Тепловизор",
        description = "Увеличивает контрастность тепловых сигнатур двигателей техники и дальность захвата цели на +20% за уровень.",
        iconName = "camera",
        baseCostWP = 2000
    )
}

data class DailyReward(
    val day: Int,
    val rewardWP: Int,
    val rewardTokens: Int,
    val specialItem: String? = null,
    val isClaimed: Boolean = false
)

data class WeeklyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val rewardWP: Int,
    val rewardTokens: Int,
    val isCompleted: Boolean = false
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val rewardWP: Int,
    val isUnlocked: Boolean = false
)

data class LeaderboardEntry(
    val rank: Int,
    val callsign: String,
    val kills: Int,
    val score: Int,
    val accuracyPct: Int,
    val division: String,
    val isCurrentPlayer: Boolean = false
)
