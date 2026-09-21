package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object MindfulChimeHelper {

    fun playStartChime() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 250)
                delay(300)
                toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 350)
                delay(400)
                toneGen.release()
            } catch (_: Exception) {
                // Ignore if audio hardware unavailable in headless environment
            }
        }
    }

    fun playCompletionChime() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
                toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300)
                delay(350)
                toneGen.startTone(ToneGenerator.TONE_PROP_PROMPT, 450)
                delay(500)
                toneGen.release()
            } catch (_: Exception) {
                // Ignore if audio hardware unavailable
            }
        }
    }
}
