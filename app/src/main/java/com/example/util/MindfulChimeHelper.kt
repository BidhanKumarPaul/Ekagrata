package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object MindfulChimeHelper {

    /**
     * Plays a resonant singing bowl / meditation chime using direct PCM synthesis
     * with exponential acoustic decay, guaranteed to work across all Android devices.
     */
    fun playResonantBell(frequencyHz: Double = 528.0, durationMs: Int = 1200) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    // Acoustic decay envelope
                    val envelope = exp(-3.2 * t)
                    // Fundamental harmonic + warm overtone
                    val sample = 0.75 * sin(2.0 * PI * frequencyHz * t) +
                            0.25 * sin(2.0 * PI * (frequencyHz * 2.75) * t)
                    buffer[i] = (sample * envelope * Short.MAX_VALUE * 0.85).toInt().toShort()
                }

                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(buffer.size * 2)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        buffer.size * 2,
                        AudioTrack.MODE_STATIC
                    )
                }

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                delay(durationMs.toLong() + 100L)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Fallback to ToneGenerator on STREAM_MUSIC
                fallbackTone(ToneGenerator.TONE_DTMF_0, durationMs)
            }
        }
    }

    private fun fallbackTone(toneType: Int, durationMs: Int) {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen.startTone(toneType, durationMs)
            CoroutineScope(Dispatchers.Default).launch {
                delay(durationMs.toLong() + 50L)
                toneGen.release()
            }
        } catch (_: Exception) {
            // Hardware muted or audio service unavailable
        }
    }

    fun playStartChime() {
        playResonantBell(frequencyHz = 432.0, durationMs = 1000)
    }

    fun playCompletionChime() {
        CoroutineScope(Dispatchers.Default).launch {
            playResonantBell(frequencyHz = 528.0, durationMs = 900)
            delay(400)
            playResonantBell(frequencyHz = 660.0, durationMs = 1400)
        }
    }

    fun playMotivationalChime() {
        CoroutineScope(Dispatchers.Default).launch {
            playResonantBell(frequencyHz = 440.0, durationMs = 600)
            delay(250)
            playResonantBell(frequencyHz = 554.37, durationMs = 600)
            delay(250)
            playResonantBell(frequencyHz = 659.25, durationMs = 1200)
        }
    }
}
