package com.jarvis.nchat.core.call

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRingtonePlayer @Inject constructor(@ApplicationContext private val context: Context) {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    fun start() {
        if (ringtone?.isPlaying == true) return
        val uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(context, uri).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) isLooping = true
            play()
        }
        vibrator = context.getSystemService(Vibrator::class.java)
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 1000, 1000), 0))
    }

    fun stop() {
        ringtone?.stop()
        vibrator?.cancel()
    }
}