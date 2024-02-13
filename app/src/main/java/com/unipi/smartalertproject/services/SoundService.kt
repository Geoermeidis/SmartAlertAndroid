package com.unipi.smartalertproject.services

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer

class SoundService() {

    companion object {
        @Volatile
        private var instance: SoundService? = null

        fun getInstance(): SoundService {
            if (instance == null) {
                synchronized(SoundService::class.java) {
                    if (instance == null) {
                        instance = SoundService()
                    }
                }
            }
            return instance!!
        }
    }

    private var mediaPlayer: MediaPlayer? = null

    fun playSound(context: Context, soundResource: Int, completion: (() -> Unit)? = null) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, soundResource)
            // mediaPlayer!!.isLooping = true // Make sure it loops until stop is called
        }

        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?

        if (audioManager != null) {
            val ringerMode = audioManager.ringerMode
            if (ringerMode != AudioManager.RINGER_MODE_SILENT) {
                mediaPlayer!!.start()
                if (completion != null) {
                    mediaPlayer!!.setOnCompletionListener { completion() }
                }
            }
        }
    }

    fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}