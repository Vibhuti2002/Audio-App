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


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mediaPlayerFlow = MediaPlayerFlow()
        binding.seekBar.progress = 0


        binding.viewTrack1.setOnClickListener {
            playAudio("Drop It.mp3", mediaPlayerFlow)

        }
        binding.viewTrack2.setOnClickListener {
            playAudio("Cooking.mp3", mediaPlayerFlow)
            binding.seekBar.max = mediaPlayerFlow.duration()
        }
        binding.viewTrack3.setOnClickListener {
            playAudio("House Vibe.mp3", mediaPlayerFlow)
            binding.seekBar.max = mediaPlayerFlow.duration()
        }
        binding.bPause.setOnClickListener { 
            mediaPlayerFlow.pause()
//            binding.bPause.visibility = View.INVISIBLE
//            binding.bPlay.visibility = View.VISIBLE
        }

        binding.bPlay.setOnClickListener {
            mediaPlayerFlow.resume()
//            binding.bPlay.visibility = View.INVISIBLE
//            binding.bPause.visibility = View.VISIBLE
        }

        binding.seekBar.progress = mediaPlayerFlow.seekProgress(binding.seekBar)
//        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
//
//            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
//                if(p2){
//                    mediaPlayerFlow.seekTo(p1)
//                }
//            }
//
//            override fun onStartTrackingTouch(p0: SeekBar?) {
//            }
//
//            override fun onStopTrackingTouch(p0: SeekBar?) {
//
//            }
//
//        })


    }



    private fun playAudio(audio : String, mediaPlayerFlow : MediaPlayerFlow){
        if (!mediaPlayerFlow.isPlaying()) {
            val cacheDir = this.cacheDir
            val audioFile = File(cacheDir, audio)
            if (audioFile.exists()) {
                val audioUri = Uri.fromFile(audioFile)
                CoroutineScope(Dispatchers.Main).launch {
                    mediaPlayerFlow.play(this@MainActivity, audioUri).collect {
                    }
                }
            } else {
                saveAudioFileFromFirebaseToCache(mediaPlayerFlow, this, audio)
            }
            binding.seekBar.max = mediaPlayerFlow.duration()
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
//    private fun playAudio(count: Int) {
//        mediaPlayer?.stop()
//        mediaPlayer = null
//
//        if(mediaPlayer==null){
//            if (count==0){
//                    storageRef = storage.reference.child("Drop it.mp3")
//                val audioFile = File(cacheDir, "https://firebasestorage.googleapis.com/v0/b/audio-app-8356e.appspot.com/o/House%20Vibe.mp3?alt=media&token=577565da-967f-4405-8d49-10ca1ddfa509")
//                storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener{
//                    bytes -> try {
//                        val fos = FileOutputStream(audioFile)
//                    fos.write(bytes)
//                    fos.close()
//                    }catch (e:IOException){
//                        e.printStackTrace()
//                    }
//                    Log.d(TAG, "Success")
//                }.addOnFailureListener{
//                    exception -> //Handle failure
//                    Log.d(TAG, "Failed")
//                }
////                uri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/audio-app-8356e.appspot.com/o/Drop%20It.mp3?alt=media&token=9474708b-55d4-407f-b477-737cb3aa4658")
//                mediaPlayer = MediaPlayer.create(this, Uri.fromFile(audioFile))
////                mediaPlayer!!.setDataSource(this, audioFile.toUri())
//                mediaPlayer!!.prepare()
//
//                binding.seekBar.max = mediaPlayer!!.duration
//            }
//
//            if (count==1){
//                storageRef = FirebaseStorage.getInstance().reference.child("Cooking.mp3")
//                uri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/audio-app-8356e.appspot.com/o/Cooking.mp3?alt=media&token=4aebd07b-3573-4f68-b7d5-9518a7bdef3d")
//                mediaPlayer = MediaPlayer.create(this, uri)
//
//            }
//            if (count==2){
//                storageRef = FirebaseStorage.getInstance().reference.child("House Vibe.mp3")
//                uri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/audio-app-8356e.appspot.com/o/House%20Vibe.mp3?alt=media&token=577565da-967f-4405-8d49-10ca1ddfa509")
//                mediaPlayer = MediaPlayer.create(this, uri)
//            }
//        }
//
//        mediaPlayer?.start()
//        binding.bPause.visibility = View.VISIBLE
//        runnable = Runnable {
//            binding.seekBar.progress = mediaPlayer!!.currentPosition
//            handler.postDelayed(runnable, 1000)
//        }
//        handler.postDelayed(runnable, 1000)
//        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
//            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
//                if(p2){
//                    mediaPlayer!!.seekTo(p1)
//                }
//            }
//
//            override fun onStartTrackingTouch(p0: SeekBar?) {
//
//            }
//
//            override fun onStopTrackingTouch(p0: SeekBar?) {
//
//            }
//
//        })
//        Toast.makeText(this, "Audio Started Playing", Toast.LENGTH_SHORT).show()
//    }
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
                        Toast.makeText(context,"Be focused",Toast.LENGTH_SHORT).show()
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