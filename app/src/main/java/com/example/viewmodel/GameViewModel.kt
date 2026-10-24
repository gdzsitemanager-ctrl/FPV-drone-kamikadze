package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.data.GamePreferencesRepository
import com.example.model.Achievement
import com.example.model.BatteryTypeId
import com.example.model.DailyReward
import com.example.model.DroneFrameId
import com.example.model.ExplosionEffect
import com.example.model.FlakBullet
import com.example.model.HitZone
import com.example.model.LeaderboardEntry
import com.example.model.ModuleType
import com.example.model.MotorTypeId
import com.example.model.MultiplayerPilot
import com.example.model.PlayerDroneEquipment
import com.example.model.SamMissile
import com.example.model.SkinId
import com.example.model.TargetEntity
import com.example.model.TargetType
import com.example.model.WarheadType
import com.example.model.WeeklyChallenge
import com.example.physics.FlightPhysics
import com.example.physics.Vector3D
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class AppScreen {
    HANGAR,
    FLIGHT,
    MULTIPLAYER,
    UPGRADES,
    CHALLENGES,
    LEADERBOARD
}

data class MissionResult(
    val isSuccess: Boolean,
    val targetHitTitle: String,
    val hitZone: HitZone?,
    val damageDealt: Float,
    val wpEarned: Int,
    val scoreEarned: Int,
    val wasShotDown: Boolean,
    val details: String
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val repository = GamePreferencesRepository(application)
    val audioEngine = AudioEngine()

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.HANGAR)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Economy state
    private val _warPoints = MutableStateFlow(repository.warPoints)
    val warPoints: StateFlow<Int> = _warPoints.asStateFlow()

    private val _tokens = MutableStateFlow(repository.tokens)
    val tokens: StateFlow<Int> = _tokens.asStateFlow()

    private val _equipment = MutableStateFlow(repository.loadEquipment())
    val equipment: StateFlow<PlayerDroneEquipment> = _equipment.asStateFlow()

    // Flight physics & entities
    var physics: FlightPhysics = createPhysics()
        private set

    private val _targets = MutableStateFlow<List<TargetEntity>>(emptyList())
    val targets: StateFlow<List<TargetEntity>> = _targets.asStateFlow()

    private val _samMissiles = MutableStateFlow<List<SamMissile>>(emptyList())
    val samMissiles: StateFlow<List<SamMissile>> = _samMissiles.asStateFlow()

    private val _flakBullets = MutableStateFlow<List<FlakBullet>>(emptyList())
    val flakBullets: StateFlow<List<FlakBullet>> = _flakBullets.asStateFlow()

    private val _explosions = MutableStateFlow<List<ExplosionEffect>>(emptyList())
    val explosions: StateFlow<List<ExplosionEffect>> = _explosions.asStateFlow()

    private val _competitors = MutableStateFlow<List<MultiplayerPilot>>(emptyList())
    val competitors: StateFlow<List<MultiplayerPilot>> = _competitors.asStateFlow()

    // Controls
    val throttleInput = MutableStateFlow(0.35f)
    val pitchInput = MutableStateFlow(0f)
    val rollInput = MutableStateFlow(0f)
    val yawInput = MutableStateFlow(0f)
    val isArmed = MutableStateFlow(true)
    val isThermalMode = MutableStateFlow(false)

    // Mission status
    private val _missionResult = MutableStateFlow<MissionResult?>(null)
    val missionResult: StateFlow<MissionResult?> = _missionResult.asStateFlow()

    val isMultiplayerSortie = MutableStateFlow(false)
    val multiplayerRoomName = MutableStateFlow("Операция «Шторм ПВО»")

    // Daily & weekly
    private val _dailyRewards = MutableStateFlow<List<DailyReward>>(emptyList())
    val dailyRewards: StateFlow<List<DailyReward>> = _dailyRewards.asStateFlow()

    private val _weeklyChallenges = MutableStateFlow<List<WeeklyChallenge>>(emptyList())
    val weeklyChallenges: StateFlow<List<WeeklyChallenge>> = _weeklyChallenges.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    private var gameLoopJob: Job? = null

    init {
        audioEngine.start()
        refreshChallengesAndAchievements()
        refreshLeaderboard()
    }

    fun navigateTo(screen: AppScreen) {
        if (screen == AppScreen.FLIGHT) {
            startFlight(isMultiplayer = false)
        } else {
            stopFlightLoop()
            _currentScreen.value = screen
        }
    }

    private fun createPhysics(): FlightPhysics {
        val eq = _equipment.value
        val motorTier = repository.getModuleLevel(ModuleType.MOTOR_THRUST)
        val battTier = repository.getModuleLevel(ModuleType.BATTERY_CAPACITY)
        val ewTier = repository.getModuleLevel(ModuleType.EW_SHIELDING)

        return FlightPhysics(
            equipment = eq,
            thrustMultiplier = 1.0f + (motorTier - 1) * 0.12f,
            batteryEfficiency = 1.0f + (battTier - 1) * 0.15f,
            ewResistanceMultiplier = 1.0f + (ewTier - 1) * 0.20f
        )
    }

    fun startFlight(isMultiplayer: Boolean) {
        stopFlightLoop()
        isMultiplayerSortie.value = isMultiplayer
        physics = createPhysics()
        _missionResult.value = null
        throttleInput.value = 0.40f
        pitchInput.value = 0f
        rollInput.value = 0f
        yawInput.value = 0f
        isArmed.value = true

        // Spawn targets
        val list = mutableListOf<TargetEntity>()
        // 1. Pantsir-S1 Complex (Main Boss / Primary Target)
        list.add(
            TargetEntity(
                id = "pantsir_1",
                type = TargetType.PANTSIR_S1,
                position = Vector3D(0f, 0f, 420f),
                rotationY = PI.toFloat()
            )
        )
        // 2. Tank T-72 with reactive armor
        list.add(
            TargetEntity(
                id = "tank_1",
                type = TargetType.TANK_T72,
                position = Vector3D(-90f, 0f, 310f),
                rotationY = 0.4f
            )
        )
        // 3. Ammo Depot
        list.add(
            TargetEntity(
                id = "ammo_depot_1",
                type = TargetType.AMMO_DEPOT,
                position = Vector3D(110f, 0f, 380f)
            )
        )
        // 4. Infantry Dugout
        list.add(
            TargetEntity(
                id = "bunker_1",
                type = TargetType.INFANTRY_BUNKER,
                position = Vector3D(-160f, 0f, 220f)
            )
        )
        // 5. Early Warning Radar Mast
        list.add(
            TargetEntity(
                id = "radar_1",
                type = TargetType.RADAR_MAST,
                position = Vector3D(180f, 0f, 520f)
            )
        )
        // 6. Enemy Recon Drone
        list.add(
            TargetEntity(
                id = "recon_1",
                type = TargetType.ENEMY_RECON_DRONE,
                position = Vector3D(40f, 45f, 260f)
            )
        )
        _targets.value = list

        _samMissiles.value = emptyList()
        _flakBullets.value = emptyList()
        _explosions.value = emptyList()

        // Multiplayer pilots
        if (isMultiplayer) {
            _competitors.value = listOf(
                MultiplayerPilot("Стриж_22", false, Vector3D(-35f, 18f, 40f), 1, 1400, true, 28),
                MultiplayerPilot("Viper_Pro", false, Vector3D(45f, 22f, 55f), 2, 2850, true, 34),
                MultiplayerPilot("Valkyrie_UA", false, Vector3D(-65f, 14f, 85f), 0, 800, true, 19),
                MultiplayerPilot(repository.callsign, true, physics.position, 0, 0, true, 18)
            )
        } else {
            _competitors.value = emptyList()
        }

        _currentScreen.value = AppScreen.FLIGHT

        // Start 60 FPS Game Loop
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()

            while (isActive) {
                val now = System.nanoTime()
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                lastTime = now

                updateGameTick(dt)
                delay(16) // ~60 fps
            }
        }
    }

    private fun updateGameTick(dt: Float) {
        if (_missionResult.value != null) return

        // 1. Update drone flight physics
        val ewDistances = _targets.value
            .filter { !it.isDestroyed && it.type.hasEwJammer }
            .map { it.position.distanceTo(physics.position) }

        physics.update(
            dt = dt,
            throttleInput = throttleInput.value,
            pitchInput = pitchInput.value,
            rollInput = rollInput.value,
            yawInput = yawInput.value,
            isArmed = isArmed.value,
            ewJammerDistances = ewDistances
        )

        // Audio sync
        audioEngine.throttle = throttleInput.value
        audioEngine.isArmed = isArmed.value
        audioEngine.ewInterference = physics.ewInterferenceLevel

        // 2. Update Targets & Air Defense AI
        val nowMs = System.currentTimeMillis()
        val currentTargets = _targets.value.toMutableList()
        val currentMissiles = _samMissiles.value.toMutableList()
        val currentFlak = _flakBullets.value.toMutableList()

        for (target in currentTargets) {
            if (target.isDestroyed) continue

            // Rotate radar dish
            target.radarAngle += 2.2f * dt

            if (target.type.hasAirDefense) {
                val dist = target.position.distanceTo(physics.position)

                // Flak Autocannon AI (triggers within 460m)
                if (dist < 460f && nowMs - target.lastShotTimeMs > 480L) {
                    target.lastShotTimeMs = nowMs
                    audioEngine.playFlakBurst()

                    // Fire 3-round 30mm tracer burst
                    val dir = (physics.position - target.position).normalized()
                    val spread = Vector3D(
                        (Random.nextFloat() - 0.5f) * 0.08f,
                        (Random.nextFloat() - 0.5f) * 0.08f,
                        (Random.nextFloat() - 0.5f) * 0.08f
                    )
                    val bulletVel = (dir + spread).normalized() * 320f
                    currentFlak.add(FlakBullet(target.position + Vector3D(0f, 2.5f, 0f), bulletVel))
                }

                // SAM Missile Launch AI (if drone is high enough > 14m and within 650m)
                if (dist < 650f && physics.position.y > 14f && currentMissiles.size < 2) {
                    if (nowMs - target.lastMissileTimeMs > 4500L) {
                        target.lastMissileTimeMs = nowMs
                        audioEngine.playMissileLaunch()
                        audioEngine.playBeep()
                        vibrateAlert(120)

                        val missilePos = target.position + Vector3D(1.2f, 3.2f, 0f)
                        val missileVel = (physics.position - missilePos).normalized() * 140f
                        currentMissiles.add(
                            SamMissile(
                                id = "sam_${Random.nextInt()}",
                                position = missilePos,
                                velocity = missileVel
                            )
                        )
                    }
                }
            }
        }

        // 3. Update SAM Missiles (Homing towards drone)
        val activeMissiles = mutableListOf<SamMissile>()
        for (m in currentMissiles) {
            m.lifeSec -= dt
            if (m.lifeSec > 0f) {
                // Proportional navigation / lead homing
                val toDrone = (physics.position - m.position).normalized()
                val speed = m.velocity.length()
                val newVel = (m.velocity * 0.94f + toDrone * (speed * 0.08f)).normalized() * speed
                m.velocity = newVel
                m.position = m.position + m.velocity * dt

                // Append smoke trail
                m.smokeTrail.add(m.position)
                if (m.smokeTrail.size > 22) m.smokeTrail.removeAt(0)

                // Check collision with drone
                val hitDist = m.position.distanceTo(physics.position)
                if (hitDist < 3.2f) {
                    triggerDroneDestroyed("Сбит зенитной ракетой ЗРПК «Панцирь»!")
                    return
                }
                activeMissiles.add(m)
            }
        }
        _samMissiles.value = activeMissiles

        // 4. Update 30mm Flak Bullets
        val activeFlak = mutableListOf<FlakBullet>()
        for (b in currentFlak) {
            b.lifeSec -= dt
            if (b.lifeSec > 0f) {
                b.position = b.position + b.velocity * dt
                val hitDist = b.position.distanceTo(physics.position)
                if (hitDist < 2.0f) {
                    triggerDroneDestroyed("Сбит огнем 30-мм спаренной автоматической пушки 2А38М!")
                    return
                }
                activeFlak.add(b)
            }
        }
        _flakBullets.value = activeFlak

        // 5. Check Drone Collision with Targets (Kamikaze Impact!)
        for (target in currentTargets) {
            if (target.isDestroyed) continue

            val dist = target.position.distanceTo(physics.position)
            // Bounding collision radius
            val hitThreshold = when (target.type) {
                TargetType.PANTSIR_S1 -> 4.5f
                TargetType.TANK_T72 -> 3.8f
                TargetType.AMMO_DEPOT -> 6.5f
                TargetType.INFANTRY_BUNKER -> 4.8f
                TargetType.RADAR_MAST -> 3.5f
                TargetType.ENEMY_RECON_DRONE -> 2.2f
            }

            if (dist < hitThreshold) {
                triggerTargetImpact(target)
                return
            }
        }

        // 6. Ground crash check
        if (physics.position.y <= 0.4f && physics.velocity.length() > 6.0f) {
            triggerGroundCrash()
            return
        }

        // 7. Update Explosion Effects
        val activeExplosions = _explosions.value.mapNotNull { exp ->
            exp.life -= dt * 1.5f
            exp.radius += (exp.maxRadius - exp.radius) * 6.0f * dt
            if (exp.life > 0f) exp else null
        }
        _explosions.value = activeExplosions

        _targets.value = currentTargets
    }

    private fun triggerTargetImpact(target: TargetEntity) {
        audioEngine.playExplosion()
        vibrateExplosion()

        // Calculate hit zone based on impact point relative to target
        val relPos = physics.position - target.position
        val warhead = _equipment.value.warhead
        val warheadYieldLvl = repository.getModuleLevel(ModuleType.WARHEAD_YIELD)
        val yieldMult = 1.0f + (warheadYieldLvl - 1) * 0.15f

        val hitZone: HitZone = when (target.type) {
            TargetType.PANTSIR_S1 -> {
                if (relPos.y > 2.8f) HitZone.RADAR_DISH
                else if (kotlin.math.abs(relPos.x) > 1.2f) HitZone.AMMO_RACK
                else HitZone.HULL
            }
            TargetType.TANK_T72 -> {
                if (relPos.z < -1.0f) HitZone.ENGINE_DECK
                else if (relPos.y > 1.8f) HitZone.TURRET
                else if (warhead == WarheadType.TANDEM_PG) HitZone.HULL // Tandem overcomes ERA
                else HitZone.REACTIVE_ARMOR
            }
            TargetType.AMMO_DEPOT -> HitZone.AMMO_RACK
            else -> HitZone.HULL
        }

        // Calculate damage
        val baseDamage = when (warhead) {
            WarheadType.PG_7VL -> 260f
            WarheadType.VOG_17 -> 160f
            WarheadType.TBG_7V -> 320f
            WarheadType.TANDEM_PG -> 360f
        } * yieldMult

        val finalDamage = baseDamage * hitZone.multiplier
        target.hp -= finalDamage
        target.isDestroyed = true

        val isCatastrophic = hitZone == HitZone.AMMO_RACK || hitZone == HitZone.RADAR_DISH
        val earnedWP = (target.type.baseRewardWP * hitZone.multiplier).toInt()
        val earnedScore = earnedWP * 2

        // Add explosion at target
        _explosions.value = _explosions.value + ExplosionEffect(
            position = target.position,
            radius = 2.0f,
            maxRadius = if (isCatastrophic) 24f else 14f,
            life = 1.5f,
            isCatastrophic = isCatastrophic
        )

        // Award currency & update stats
        repository.warPoints += earnedWP
        _warPoints.value = repository.warPoints
        repository.totalKills += 1
        if (target.type == TargetType.PANTSIR_S1) {
            repository.pantsirKills += 1
        }

        // Update weekly challenges
        updateChallengeProgress("weekly_pantsir", if (target.type == TargetType.PANTSIR_S1) 1 else 0)
        updateChallengeProgress("weekly_engine", if (hitZone == HitZone.ENGINE_DECK) 1 else 0)
        updateChallengeProgress("weekly_ew", if (physics.ewInterferenceLevel > 0.3f) 1 else 0)
        updateChallengeProgress("weekly_acro", if (_equipment.value.isAcroMode) 1 else 0)

        // Update achievements
        updateAchievementProgress("ach_first_kill", 1)
        if (target.type == TargetType.PANTSIR_S1) {
            updateAchievementProgress("ach_pantsir_hunter", 1)
        }
        if (isCatastrophic) {
            updateAchievementProgress("ach_ammo_detonation", 1)
        }

        _missionResult.value = MissionResult(
            isSuccess = true,
            targetHitTitle = target.type.title,
            hitZone = hitZone,
            damageDealt = finalDamage,
            wpEarned = earnedWP,
            scoreEarned = earnedScore,
            wasShotDown = false,
            details = "${hitZone.title} Нанесен урон ${finalDamage.toInt()} HP. Награда: +$earnedWP WP"
        )
    }

    private fun triggerDroneDestroyed(reason: String) {
        audioEngine.playExplosion()
        vibrateExplosion()

        _explosions.value = _explosions.value + ExplosionEffect(
            position = physics.position,
            radius = 1.5f,
            maxRadius = 10f,
            life = 1.2f,
            isCatastrophic = false
        )

        _missionResult.value = MissionResult(
            isSuccess = false,
            targetHitTitle = "Борт утерян",
            hitZone = null,
            damageDealt = 0f,
            wpEarned = 150, // Consolation XP
            scoreEarned = 100,
            wasShotDown = true,
            details = reason
        )
    }

    private fun triggerGroundCrash() {
        audioEngine.playExplosion()
        vibrateExplosion()

        _explosions.value = _explosions.value + ExplosionEffect(
            position = physics.position,
            radius = 1.0f,
            maxRadius = 8f,
            life = 1.0f,
            isCatastrophic = false
        )

        _missionResult.value = MissionResult(
            isSuccess = false,
            targetHitTitle = "Столкновение с землей",
            hitZone = null,
            damageDealt = 0f,
            wpEarned = 80,
            scoreEarned = 50,
            wasShotDown = false,
            details = "Дрон врезался в рельеф на скорости ${physics.speedKmh().toInt()} км/ч"
        )
    }

    private fun stopFlightLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
        audioEngine.isArmed = false
        audioEngine.throttle = 0f
    }

    // Customization & Shop
    fun equipFrame(frame: DroneFrameId) {
        if (!repository.isFrameUnlocked(frame)) {
            if (_warPoints.value >= frame.priceWP) {
                repository.warPoints -= frame.priceWP
                _warPoints.value = repository.warPoints
                repository.unlockFrame(frame)
            } else return
        }
        val updated = _equipment.value.copy(frame = frame)
        _equipment.value = updated
        repository.saveEquipment(updated)
    }

    fun equipWarhead(warhead: WarheadType) {
        if (!repository.isWarheadUnlocked(warhead)) {
            if (_warPoints.value >= warhead.priceWP) {
                repository.warPoints -= warhead.priceWP
                _warPoints.value = repository.warPoints
                repository.unlockWarhead(warhead)
            } else return
        }
        val updated = _equipment.value.copy(warhead = warhead)
        _equipment.value = updated
        repository.saveEquipment(updated)
    }

    fun equipMotor(motor: MotorTypeId) {
        val updated = _equipment.value.copy(motor = motor)
        _equipment.value = updated
        repository.saveEquipment(updated)
    }

    fun equipBattery(battery: BatteryTypeId) {
        val updated = _equipment.value.copy(battery = battery)
        _equipment.value = updated
        repository.saveEquipment(updated)
    }

    fun equipSkin(skin: SkinId) {
        if (!repository.isSkinUnlocked(skin)) {
            if (_warPoints.value >= skin.priceWP) {
                repository.warPoints -= skin.priceWP
                _warPoints.value = repository.warPoints
                repository.unlockSkin(skin)
            } else return
        }
        val updated = _equipment.value.copy(skin = skin)
        _equipment.value = updated
        repository.saveEquipment(updated)
    }

    fun toggleAcroMode() {
        val updated = _equipment.value.copy(isAcroMode = !_equipment.value.isAcroMode)
        _equipment.value = updated
        repository.saveEquipment(updated)
    }

    fun setCameraAngle(angle: Float) {
        val updated = _equipment.value.copy(cameraAngleDeg = angle.coerceIn(0f, 45f))
        _equipment.value = updated
        repository.saveEquipment(updated)
    }

    fun upgradeModule(module: ModuleType) {
        val curLevel = repository.getModuleLevel(module)
        if (curLevel >= module.maxLevel) return

        val cost = module.baseCostWP * curLevel
        if (_warPoints.value >= cost) {
            repository.warPoints -= cost
            _warPoints.value = repository.warPoints
            repository.setModuleLevel(module, curLevel + 1)
        }
    }

    fun claimDailyReward(day: Int) {
        val reward = _dailyRewards.value.find { it.day == day } ?: return
        if (reward.isClaimed) return

        repository.warPoints += reward.rewardWP
        _warPoints.value = repository.warPoints
        repository.tokens += reward.rewardTokens
        _tokens.value = repository.tokens

        repository.lastDailyClaimEpoch = System.currentTimeMillis()
        repository.dailyStreak += 1

        refreshChallengesAndAchievements()
    }

    private fun updateChallengeProgress(id: String, increment: Int) {
        if (increment <= 0) return
        val cur = repository.getChallengeProgress(id)
        repository.setChallengeProgress(id, cur + increment)
        refreshChallengesAndAchievements()
    }

    fun claimWeeklyChallenge(id: String) {
        val challenge = _weeklyChallenges.value.find { it.id == id } ?: return
        if (challenge.currentProgress < challenge.targetProgress || challenge.isCompleted) return

        repository.warPoints += challenge.rewardWP
        _warPoints.value = repository.warPoints
        repository.tokens += challenge.rewardTokens
        _tokens.value = repository.tokens
        repository.setChallengeClaimed(id)
        refreshChallengesAndAchievements()
    }

    private fun updateAchievementProgress(id: String, increment: Int) {
        val cur = repository.getAchievementProgress(id)
        repository.setAchievementProgress(id, cur + increment)
        refreshChallengesAndAchievements()
    }

    fun refreshChallengesAndAchievements() {
        // Daily rewards (7 days streak)
        val streak = repository.dailyStreak
        val days = (1..7).map { d ->
            DailyReward(
                day = d,
                rewardWP = d * 600,
                rewardTokens = if (d % 3 == 0) 5 else 2,
                specialItem = if (d == 7) "Тандемная БЧ" else null,
                isClaimed = d < streak
            )
        }
        _dailyRewards.value = days

        // Weekly Challenges
        _weeklyChallenges.value = listOf(
            WeeklyChallenge(
                id = "weekly_pantsir",
                title = "Ликвидатор Панцирей",
                description = "Уничтожьте боевые машины ЗРПК «Панцирь-С1»",
                currentProgress = repository.getChallengeProgress("weekly_pantsir").coerceAtMost(5),
                targetProgress = 5,
                rewardWP = 3500,
                rewardTokens = 15,
                isCompleted = repository.isChallengeClaimed("weekly_pantsir")
            ),
            WeeklyChallenge(
                id = "weekly_engine",
                title = "Снайпер FPV: Моторный Отсек",
                description = "Поразите моторный отсек тяжелой бронетехники с тыла",
                currentProgress = repository.getChallengeProgress("weekly_engine").coerceAtMost(3),
                targetProgress = 3,
                rewardWP = 2800,
                rewardTokens = 10,
                isCompleted = repository.isChallengeClaimed("weekly_engine")
            ),
            WeeklyChallenge(
                id = "weekly_ew",
                title = "Прорыв Сквозь РЭБ",
                description = "Уничтожьте цель внутри купола активного радиоэлектронного подавления",
                currentProgress = repository.getChallengeProgress("weekly_ew").coerceAtMost(2),
                targetProgress = 2,
                rewardWP = 3000,
                rewardTokens = 12,
                isCompleted = repository.isChallengeClaimed("weekly_ew")
            ),
            WeeklyChallenge(
                id = "weekly_acro",
                title = "Мастер Акро-Режима",
                description = "Совершите точное попадание с включенным ручным режимом ACRO",
                currentProgress = repository.getChallengeProgress("weekly_acro").coerceAtMost(1),
                targetProgress = 1,
                rewardWP = 4000,
                rewardTokens = 20,
                isCompleted = repository.isChallengeClaimed("weekly_acro")
            )
        )

        // Achievements
        _achievements.value = listOf(
            Achievement(
                id = "ach_first_kill",
                title = "Первый Контакт",
                description = "Успешно поразите первую вражескую цель дроном-камикадзе",
                currentProgress = repository.getAchievementProgress("ach_first_kill").coerceAtMost(1),
                maxProgress = 1,
                rewardWP = 1000,
                isUnlocked = repository.getAchievementProgress("ach_first_kill") >= 1
            ),
            Achievement(
                id = "ach_pantsir_hunter",
                title = "Гроза ПВО «Панцирь»",
                description = "Уничтожьте 5 зенитных ракетно-пушечных комплексов «Панцирь-С1»",
                currentProgress = repository.pantsirKills.coerceAtMost(5),
                maxProgress = 5,
                rewardWP = 5000,
                isUnlocked = repository.pantsirKills >= 5
            ),
            Achievement(
                id = "ach_ammo_detonation",
                title = "Фейерверк БК",
                description = "Вызовите вторичную катастрофическую детонацию боекомплекта",
                currentProgress = repository.getAchievementProgress("ach_ammo_detonation").coerceAtMost(3),
                maxProgress = 3,
                rewardWP = 3200,
                isUnlocked = repository.getAchievementProgress("ach_ammo_detonation") >= 3
            ),
            Achievement(
                id = "ach_veteran",
                title = "Ас Воздушного Удара",
                description = "Наберите более 15 подтвержденных уничтожений боевой техники",
                currentProgress = repository.totalKills.coerceAtMost(15),
                maxProgress = 15,
                rewardWP = 8000,
                isUnlocked = repository.totalKills >= 15
            )
        )
    }

    private fun refreshLeaderboard() {
        val playerKills = repository.totalKills
        val playerScore = playerKills * 2200 + 1500
        val playerRank = if (playerKills > 20) 2 else if (playerKills > 8) 4 else 8

        _leaderboard.value = listOf(
            LeaderboardEntry(1, "CYBER_EAGLE", 48, 114500, 96, "Призрак"),
            LeaderboardEntry(2, "Ghost_FPV", 39, 92400, 92, "Ас"),
            LeaderboardEntry(3, "PantsirSlayer", 34, 81200, 89, "Ас"),
            LeaderboardEntry(4, "NightRaven_99", 26, 61800, 85, "Ветеран"),
            LeaderboardEntry(5, "Nord_Pilot", 21, 51300, 88, "Ветеран"),
            LeaderboardEntry(6, "Buzzer_FPV", 18, 44200, 82, "Оператор"),
            LeaderboardEntry(7, "SkyWolf_01", 14, 33600, 79, "Оператор"),
            LeaderboardEntry(playerRank, repository.callsign, playerKills, playerScore, 91, "Оператор", isCurrentPlayer = true),
            LeaderboardEntry(9, "Raptor_Team", 9, 21500, 74, "Новичок"),
            LeaderboardEntry(10, "Delta_Striker", 6, 14200, 68, "Новичок")
        ).sortedBy { it.rank }
    }

    private fun vibrateExplosion() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(350, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(350)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateAlert(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        stopFlightLoop()
        audioEngine.stop()
    }
}
