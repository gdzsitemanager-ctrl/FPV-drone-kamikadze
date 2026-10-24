package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.BatteryTypeId
import com.example.model.DroneFrameId
import com.example.model.ModuleType
import com.example.model.MotorTypeId
import com.example.model.PlayerDroneEquipment
import com.example.model.SkinId
import com.example.model.WarheadType

class GamePreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pantsir_fpv_save", Context.MODE_PRIVATE)

    var warPoints: Int
        get() = prefs.getInt("key_wp", 3500)
        set(value) = prefs.edit().putInt("key_wp", value).apply()

    var tokens: Int
        get() = prefs.getInt("key_tokens", 25)
        set(value) = prefs.edit().putInt("key_tokens", value).apply()

    var callsign: String
        get() = prefs.getString("key_callsign", "Ghost_FPV") ?: "Ghost_FPV"
        set(value) = prefs.edit().putString("key_callsign", value).apply()

    var totalKills: Int
        get() = prefs.getInt("key_total_kills", 4)
        set(value) = prefs.edit().putInt("key_total_kills", value).apply()

    var pantsirKills: Int
        get() = prefs.getInt("key_pantsir_kills", 2)
        set(value) = prefs.edit().putInt("key_pantsir_kills", value).apply()

    var dailyStreak: Int
        get() = prefs.getInt("key_daily_streak", 1)
        set(value) = prefs.edit().putInt("key_daily_streak", value).apply()

    var lastDailyClaimEpoch: Long
        get() = prefs.getLong("key_last_daily", 0L)
        set(value) = prefs.edit().putLong("key_last_daily", value).apply()

    // Upgrades: Module -> Level (1..5)
    fun getModuleLevel(module: ModuleType): Int {
        return prefs.getInt("module_${module.name}", 1)
    }

    fun setModuleLevel(module: ModuleType, level: Int) {
        prefs.edit().putInt("module_${module.name}", level.coerceIn(1, module.maxLevel)).apply()
    }

    // Unlocks
    fun isSkinUnlocked(skinId: SkinId): Boolean {
        if (skinId == SkinId.SKIN_PIXEL) return true
        return prefs.getBoolean("unlocked_skin_${skinId.name}", false)
    }

    fun unlockSkin(skinId: SkinId) {
        prefs.edit().putBoolean("unlocked_skin_${skinId.name}", true).apply()
    }

    fun isFrameUnlocked(frameId: DroneFrameId): Boolean {
        if (frameId == DroneFrameId.SHMEL_7) return true
        return prefs.getBoolean("unlocked_frame_${frameId.name}", false)
    }

    fun unlockFrame(frameId: DroneFrameId) {
        prefs.edit().putBoolean("unlocked_frame_${frameId.name}", true).apply()
    }

    fun isWarheadUnlocked(warheadType: WarheadType): Boolean {
        if (warheadType == WarheadType.PG_7VL) return true
        return prefs.getBoolean("unlocked_warhead_${warheadType.name}", false)
    }

    fun unlockWarhead(warheadType: WarheadType) {
        prefs.edit().putBoolean("unlocked_warhead_${warheadType.name}", true).apply()
    }

    // Equipment
    fun loadEquipment(): PlayerDroneEquipment {
        val frameStr = prefs.getString("eq_frame", DroneFrameId.SHMEL_7.name)
        val warheadStr = prefs.getString("eq_warhead", WarheadType.PG_7VL.name)
        val motorStr = prefs.getString("eq_motor", MotorTypeId.MOTOR_2807.name)
        val batteryStr = prefs.getString("eq_battery", BatteryTypeId.LIPO_6S_4000.name)
        val skinStr = prefs.getString("eq_skin", SkinId.SKIN_PIXEL.name)
        val camAngle = prefs.getFloat("eq_cam_angle", 25f)
        val isAcro = prefs.getBoolean("eq_is_acro", false)

        val frame = runCatching { DroneFrameId.valueOf(frameStr ?: "") }.getOrDefault(DroneFrameId.SHMEL_7)
        val warhead = runCatching { WarheadType.valueOf(warheadStr ?: "") }.getOrDefault(WarheadType.PG_7VL)
        val motor = runCatching { MotorTypeId.valueOf(motorStr ?: "") }.getOrDefault(MotorTypeId.MOTOR_2807)
        val battery = runCatching { BatteryTypeId.valueOf(batteryStr ?: "") }.getOrDefault(BatteryTypeId.LIPO_6S_4000)
        val skin = runCatching { SkinId.valueOf(skinStr ?: "") }.getOrDefault(SkinId.SKIN_PIXEL)

        return PlayerDroneEquipment(
            frame = frame,
            warhead = warhead,
            motor = motor,
            battery = battery,
            skin = skin,
            cameraAngleDeg = camAngle,
            isAcroMode = isAcro
        )
    }

    fun saveEquipment(equipment: PlayerDroneEquipment) {
        prefs.edit()
            .putString("eq_frame", equipment.frame.name)
            .putString("eq_warhead", equipment.warhead.name)
            .putString("eq_motor", equipment.motor.name)
            .putString("eq_battery", equipment.battery.name)
            .putString("eq_skin", equipment.skin.name)
            .putFloat("eq_cam_angle", equipment.cameraAngleDeg)
            .putBoolean("eq_is_acro", equipment.isAcroMode)
            .apply()
    }

    fun getChallengeProgress(id: String): Int = prefs.getInt("challenge_prog_$id", 0)
    fun setChallengeProgress(id: String, value: Int) = prefs.edit().putInt("challenge_prog_$id", value).apply()

    fun isChallengeClaimed(id: String): Boolean = prefs.getBoolean("challenge_claimed_$id", false)
    fun setChallengeClaimed(id: String) = prefs.edit().putBoolean("challenge_claimed_$id", true).apply()

    fun getAchievementProgress(id: String): Int = prefs.getInt("ach_prog_$id", 0)
    fun setAchievementProgress(id: String, value: Int) = prefs.edit().putInt("ach_prog_$id", value).apply()
}
