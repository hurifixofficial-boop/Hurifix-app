package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

/** Shared sound feedback for Hurifix UI interactions. */
object SoundHelper {
    @Volatile private var enabled = true

    // Use the media stream so the phone's media volume controls this sound.
    // Keep volume high enough for a clear diagnostic click.
    private val toneGenerator: ToneGenerator? by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            Log.e("HurifixSound", "Could not initialize ToneGenerator", e)
            null
        }
    }

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun isEnabled(): Boolean = enabled

    fun queueClick() = playClick()

    fun playSFX(type: String) {
        if (!enabled) return
        val tone = when (type.lowercase()) {
            "click" -> ToneGenerator.TONE_PROP_BEEP2
            "success" -> ToneGenerator.TONE_PROP_ACK
            "error", "alert" -> ToneGenerator.TONE_PROP_NACK
            else -> ToneGenerator.TONE_PROP_BEEP2
        }
        val duration = when (type.lowercase()) {
            "click" -> 90
            "success" -> 160
            "error", "alert" -> 180
            else -> 90
        }
        try {
            val generator = toneGenerator
            if (generator == null || !generator.startTone(tone, duration)) {
                Log.w("HurifixSound", "Tone playback could not be started")
            }
        } catch (e: Exception) {
            Log.e("HurifixSound", "Tone playback failed", e)
        }
    }

    fun playClick() = playSFX("click")
    fun playSuccess() = playSFX("success")
    fun playError() = playSFX("error")
}
