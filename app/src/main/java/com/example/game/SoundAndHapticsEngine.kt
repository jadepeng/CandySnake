package com.example.game

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundAndHapticsEngine(context: Context) {
    private val appContext = context.applicationContext
    private val soundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val vibrator: Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val mgr = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            mgr?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()

    fun onTurn(hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrate(durationMs = 12L, amplitude = 45)
    }

    fun onEatCandy(soundEnabled: Boolean, hapticsEnabled: Boolean, isDouble: Boolean) {
        if (hapticsEnabled) vibrate(durationMs = 22L, amplitude = 95)
        if (soundEnabled) {
            val notes = if (isDouble) listOf(660.0 to 55, 880.0 to 70) else listOf(587.3 to 65)
            playToneSequence(notes, volume = 0.28f)
        }
    }

    fun onPowerUp(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrate(durationMs = 40L, amplitude = 150)
        if (soundEnabled) {
            playToneSequence(
                listOf(523.25 to 55, 659.25 to 55, 783.99 to 65, 1046.5 to 95),
                volume = 0.32f,
            )
        }
    }

    fun onNewRecord(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrate(durationMs = 55L, amplitude = 175)
        if (soundEnabled) {
            playToneSequence(
                listOf(587.3 to 65, 739.99 to 65, 880.0 to 75, 1174.66 to 120),
                volume = 0.34f,
            )
        }
    }

    fun onGameOver(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrate(durationMs = 120L, amplitude = 210)
        if (soundEnabled) {
            playToneSequence(
                listOf(392.0 to 90, 311.13 to 100, 233.08 to 170),
                volume = 0.30f,
            )
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        val v = vibrator ?: return
        runCatching {
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val safeAmplitude = if (v.hasAmplitudeControl()) amplitude.coerceIn(1, 255) else VibrationEffect.DEFAULT_AMPLITUDE
                v.vibrate(VibrationEffect.createOneShot(durationMs, safeAmplitude))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(durationMs)
            }
        }
    }

    private fun playToneSequence(notes: List<Pair<Double, Int>>, volume: Float) {
        soundScope.launch {
            runCatching {
                val sampleRate = 22050
                val totalSamples = notes.sumOf { (sampleRate * it.second) / 1000 }
                if (totalSamples <= 0) return@runCatching
                val pcm = ShortArray(totalSamples)
                var offset = 0

                for ((freqHz, durationMs) in notes) {
                    val count = (sampleRate * durationMs) / 1000
                    for (i in 0 until count) {
                        val t = i.toDouble() / sampleRate
                        val progress = i.toFloat() / count.coerceAtLeast(1)
                        // Smooth candy-pop envelope (quick attack, rounded decay)
                        val attack = (progress * 12f).coerceAtMost(1f)
                        val release = ((1f - progress) * 4f).coerceAtMost(1f)
                        val env = attack * release
                        val wave = sin(2.0 * PI * freqHz * t) * 0.78 +
                            sin(4.0 * PI * freqHz * t) * 0.22
                        val sample = (wave * env * volume * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        if (offset < pcm.size) {
                            pcm[offset++] = sample.toShort()
                        }
                    }
                }

                val minBuffer = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                ).coerceAtLeast(pcm.size * 2)

                val track = AudioTrack.Builder()
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
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(minBuffer)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                val totalMs = notes.sumOf { it.second }.toLong() + 40L
                kotlinx.coroutines.delay(totalMs)
                track.stop()
                track.release()
            }
        }
    }
}
