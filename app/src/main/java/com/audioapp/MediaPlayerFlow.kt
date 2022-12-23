package com.audioapp

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.widget.SeekBar
import kotlinx.coroutines.flow.flow
import java.time.Duration

class MediaPlayerFlow {
    private val mediaPlayer = MediaPlayer()
    private var currentPosition = 0
    private lateinit var runnable : Runnable
    private var handler  = Handler()

    fun play(context: Context, uri: Uri) = flow {
        mediaPlayer.setDataSource(context, uri)
        mediaPlayer.prepare()
        mediaPlayer.start()
        emit(Unit)
    }

    fun pause() {
        currentPosition = mediaPlayer.currentPosition
        mediaPlayer.pause()
    }

    fun stop() {
        mediaPlayer.stop()
        mediaPlayer.reset()
        currentPosition = 0
    }

    fun resume() {
        mediaPlayer.seekTo(currentPosition)
        mediaPlayer.start()
    }
    fun seekTo(p1: Int){
        mediaPlayer.seekTo(p1)
    }

    fun seekProgress(seekBar: SeekBar) : Int{
        runnable = Runnable {
            seekBar.progress = mediaPlayer.currentPosition
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
//        if(mediaPlayer!=null){
//            currentPosition = mediaPlayer.currentPosition / 1000
//            seekBar.progress = currentPosition
//        }
        return seekBar.progress
    }

    fun duration() : Int{
        val duration = mediaPlayer.duration
        return duration
    }
    fun isPlaying() = mediaPlayer.isPlaying
}