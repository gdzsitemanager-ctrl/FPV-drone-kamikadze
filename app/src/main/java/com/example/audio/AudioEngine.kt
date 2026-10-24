package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Real-time procedural audio engine for FPV Drone simulation.
 * Synthesizes 4-motor brushless drone sound, flak cannon fire, missile launches,
 * electronic warfare static noise, and explosion blasts using AudioTrack PCM.
 */
class AudioEngine {
    private val sampleRate = 22050
    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    // Dynamic parameters
    @Volatile var throttle: Float = 0f // 0.0 to 1.0
    @Volatile var isArmed: Boolean = false
    @Volatile var ewInterference: Float = 0f // 0.0 to 1.0
    @Volatile private var explosionTrigger: Float = 0f
    @Volatile private var flakTrigger: Float = 0f
    @Volatile private var missileTrigger: Float = 0f
    @Volatile private var beepTrigger: Float = 0f

    private var motorPhase = 0.0
    private var motorPhase2 = 0.0
    private var flakPhase = 0.0
    private var beepPhase = 0.0

    fun start() {
        if (audioTrack != null) return
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = (minBufferSize * 2).coerceAtLeast(2048)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            synthJob = scope.launch {
                val chunkSize = 512
                val buffer = ShortArray(chunkSize)

                while (isActive) {
                    fillAudioBuffer(buffer, chunkSize)
                    audioTrack?.write(buffer, 0, chunkSize)
                }
            }
        } catch (e: Exception) {
            Log.e("AudioEngine", "Failed to start AudioTrack", e)
        }
    }

    private fun fillAudioBuffer(buffer: ShortArray, size: Int) {
        val armed = isArmed
        val thr = throttle.coerceIn(0f, 1f)
        val ew = ewInterference.coerceIn(0f, 1f)
        val baseFreq = 160.0 + thr * 640.0 // Motor whine pitch
        val propFreq = baseFreq * 2.33 // Prop wash harmonics
        val dt1 = 2.0 * PI * baseFreq / sampleRate
        val dt2 = 2.0 * PI * propFreq / sampleRate

        for (i in 0 until size) {
            var sample = 0f

            // 1. Motor synthesis
            if (armed) {
                motorPhase += dt1
                motorPhase2 += dt2
                if (motorPhase > 2.0 * PI) motorPhase -= 2.0 * PI
                if (motorPhase2 > 2.0 * PI) motorPhase2 -= 2.0 * PI

                val tone1 = sin(motorPhase).toFloat()
                val tone2 = sin(motorPhase2).toFloat() * 0.4f
                val jitter = (Random.nextFloat() - 0.5f) * 0.15f * thr
                val motorVol = 0.22f + thr * 0.45f
                sample += (tone1 * 0.7f + tone2 * 0.3f + jitter) * motorVol
            }

            // 2. EW Static noise
            if (ew > 0.05f) {
                val whiteNoise = (Random.nextFloat() * 2f - 1f)
                sample += whiteNoise * (ew * 0.35f)
            }

            // 3. Explosion rumble
            if (explosionTrigger > 0.001f) {
                val blast = (Random.nextFloat() * 2f - 1f) * explosionTrigger
                sample += blast * 0.8f
                explosionTrigger *= 0.9992f
            }

            // 4. Flak cannon bursts
            if (flakTrigger > 0.001f) {
                flakPhase += 2.0 * PI * 85.0 / sampleRate
                val flakTone = sin(flakPhase).toFloat()
                val noise = (Random.nextFloat() * 2f - 1f) * 0.5f
                sample += (flakTone + noise) * flakTrigger * 0.7f
                flakTrigger *= 0.9985f
            }

            // 5. Missile launch whoosh
            if (missileTrigger > 0.001f) {
                val hiss = (Random.nextFloat() * 2f - 1f) * missileTrigger * 0.4f
                sample += hiss
                missileTrigger *= 0.999f
            }

            // 6. Beep (warning)
            if (beepTrigger > 0.001f) {
                beepPhase += 2.0 * PI * 1800.0 / sampleRate
                val beep = sin(beepPhase).toFloat() * 0.3f
                sample += beep * beepTrigger
                beepTrigger *= 0.998f
            }

            val clamped = sample.coerceIn(-1.0f, 1.0f)
            buffer[i] = (clamped * 32767).toInt().toShort()
        }
    }

    fun playExplosion() {
        explosionTrigger = 1.0f
    }

    fun playFlakBurst() {
        flakTrigger = 0.9f
    }

    fun playMissileLaunch() {
        missileTrigger = 0.8f
    }

    fun playBeep() {
        beepTrigger = 0.9f
    }

    fun stop() {
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
