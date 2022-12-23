package com.audioapp

import android.content.ContentValues.TAG
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toUri
import com.audioapp.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var count = 0
    private var isPaused : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mediaPlayerFlow = MediaPlayerFlow()
        binding.seekBar.progress = 0


        binding.viewTrack1.setOnClickListener {
            count = 1
            playAudio("Drop It.mp3", mediaPlayerFlow)

        }
        binding.viewTrack2.setOnClickListener {
            count =2
            playAudio("Cooking.mp3", mediaPlayerFlow)
        }
        binding.viewTrack3.setOnClickListener {
            count = 3
            playAudio("House Vibe.mp3", mediaPlayerFlow)
        }
        binding.bPause.setOnClickListener {
            isPaused = true
            mediaPlayerFlow.pause()
            binding.bPause.visibility = View.INVISIBLE
            binding.bPlay.visibility = View.VISIBLE
        }
        binding.bPlay.setOnClickListener {
            if(!mediaPlayerFlow.isPlaying()){
                playAudio("Drop It.mp3", mediaPlayerFlow)
            }
            mediaPlayerFlow.resume()
            binding.bPlay.visibility = View.INVISIBLE
            binding.bPause.visibility = View.VISIBLE
        }
        binding.bNext.setOnClickListener {
            if(count == 3){
                Toast.makeText(this, "This is last track", Toast.LENGTH_SHORT).show()
            }
            if(count==2){
                count += 1
                playAudio("House Vibe.mp3", mediaPlayerFlow)
            }
            if(count == 1){
                count += 1
                playAudio("Cooking.mp3", mediaPlayerFlow)
            }
        }

        binding.bPrevious.setOnClickListener {
            if(count == 1){
                Toast.makeText(this, "This is first track", Toast.LENGTH_SHORT).show()
            }
            if(count == 2){
                count =1
                playAudio("Drop It.mp3", mediaPlayerFlow)
            }
            if(count==3){
                count = 2
                playAudio("Cooking.mp3", mediaPlayerFlow)
            }
        }
        binding.seekBar.progress = mediaPlayerFlow.seekProgress(binding.seekBar)

    }



    private fun playAudio(audio : String, mediaPlayerFlow : MediaPlayerFlow){
        if(mediaPlayerFlow.isPlaying() || isPaused){
            mediaPlayerFlow.reset()
        }
        if (!mediaPlayerFlow.isPlaying()) {
            val cacheDir = this.cacheDir
            val audioFile = File(cacheDir, audio)
            if (audioFile.exists()) {
                val audioUri = Uri.fromFile(audioFile)
                CoroutineScope(Dispatchers.Main).launch {
                    mediaPlayerFlow.play(this@MainActivity, audioUri).collect {
                        if(binding.bPlay.visibility == View.VISIBLE){
                            binding.bPause.visibility = View.VISIBLE
                            binding.bPlay.visibility = View.INVISIBLE
                        }
                        binding.seekBar.max = mediaPlayerFlow.duration()
                        binding.seekBar.progress = mediaPlayerFlow.seekProgress(binding.seekBar)
                        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{

                            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                                if(p2){
                                    mediaPlayerFlow.seekTo(p1)
                                }
                            }
                            override fun onStartTrackingTouch(p0: SeekBar?) {
                            }
                            override fun onStopTrackingTouch(p0: SeekBar?) {
                            }
                        })
                    }
                }
            } else {
                saveAudioFileFromFirebaseToCache(mediaPlayerFlow, this, audio)
            }

        }

    }
    private fun saveAudioFileFromFirebaseToCache(
        mediaPlayerFlow: MediaPlayerFlow,
        context: Context,
        audio : String
    ) {
        val storage = FirebaseStorage.getInstance()
        val audioRef = storage.getReference(audio)
        val cacheDir = context.cacheDir
        val audioFile = File(cacheDir, audio)
        val audioUri = Uri.fromFile(audioFile)

        val progress = context.progressDialog()
        progress.show()
        audioRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            try {
                val fos = FileOutputStream(audioFile)
                fos.write(bytes)
                fos.close()
                progress.dismiss()
                CoroutineScope(Dispatchers.Main).launch {
                    mediaPlayerFlow.play(context, audioUri).collect {
                        if(binding.bPlay.visibility == View.VISIBLE){
                            binding.bPause.visibility = View.VISIBLE
                            binding.bPlay.visibility = View.INVISIBLE
                        }
                        binding.seekBar.max = mediaPlayerFlow.duration()
                        binding.seekBar.progress = mediaPlayerFlow.seekProgress(binding.seekBar)
                        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{

                            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                                if(p2){
                                    mediaPlayerFlow.seekTo(p1)
                                }
                            }
                            override fun onStartTrackingTouch(p0: SeekBar?) {
                            }
                            override fun onStopTrackingTouch(p0: SeekBar?) {
                            }
                        })
                    }
                }
            } catch (e: IOException) {
                progress.dismiss()
                e.printStackTrace()
            }
        }.addOnFailureListener { exception ->
            progress.dismiss()
            Log.e(TAG, "saveAudioFileFromFirebaseToCache: $exception")
        }
    }
}