package com.example.contentproviderformusic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import com.example.contentproviderformusic.MainActivity.Companion.currentSong
import com.example.contentproviderformusic.MainActivity.Companion.indexSong
import com.example.contentproviderformusic.MainActivity.Companion.musicService

class NotificationReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            //only play next or prev song, when music list contains more than one song
            MusicApplication.PREVIOUS -> {
                context?.let { ctx -> prevSong(ctx) }
            }
            MusicApplication.PLAY -> if(MainActivity.isPlaying) pauseMusic() else playMusic()
            MusicApplication.NEXT -> if(MainViewModel.songs.size > 1) context?.let { nextSong(context = it) }
            MusicApplication.EXIT -> { exitApplication() }
        }
    }
    private fun playMusic(){
        MainActivity.isPlaying = true
        musicService?.mMediaPlayer?.start()
        musicService?.startCustomForegroundService(MainActivity.currentSong!!,R.drawable.pause_icon)
        //PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
       // for handling app crash during notification play - pause btn (While app opened through intent)
       // try{ NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon) }catch (_: Exception){}
    }

    private fun pauseMusic(){
        MainActivity.isPlaying = false
        musicService?.mMediaPlayer?.pause()
        musicService?.startCustomForegroundService(MainActivity.currentSong!!, R.drawable.play_icon)
       //for handling app crash during notification play - pause btn (While app opened through intent)
     //   try{ NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.play_icon) }catch (_: Exception){}
    }

    private fun prevSong(context: Context){
        if(MainViewModel.songs.size > 1) {
            musicService?.releaseMediaPlayer()
            currentSong = null
            indexSong.getAndDecrement()
            if (indexSong.get() > -1 && indexSong.get() < MainViewModel.songs.size - 1) {
                currentSong = MainViewModel.songs[indexSong.get()]
                currentSong?.let { song ->
                    musicService?.mMediaPlayer = MediaPlayer.create(context, song.uri)
                    musicService?.mMediaPlayer?.start()
                    //  playSelectedSong(song.uri)
                    musicService?.startCustomForegroundService(song)
                }
            }
//        setSongPosition(increment = increment)
      //  MainActivity.musicService?.releaseMediaPlayer()
      //  MainActivity.musicService?.mMediaPlayer?.start()
//        Glide.with(context)
//            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
//            .apply(RequestOptions().placeholder(R.drawable.music_player_icon_slash_screen).centerCrop())
//            .into(PlayerActivity.binding.songImgPA)
//        PlayerActivity.binding.songNamePA.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
//        Glide.with(context)
//            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
//            .apply(RequestOptions().placeholder(R.drawable.music_player_icon_slash_screen).centerCrop())
//            .into(NowPlaying.binding.songImgNP)
//        NowPlaying.binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
//        playMusic()
//        PlayerActivity.fIndex = favouriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
//        if(PlayerActivity.isFavourite) PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.favourite_icon)
//        else PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.favourite_empty_icon)
    }
    }

    private fun nextSong(context: Context) {
        if (MainViewModel.songs.size > 1) {
            musicService?.releaseMediaPlayer()
            currentSong = null
            indexSong.getAndIncrement()
            if (indexSong.get() > -1 && indexSong.get() < MainViewModel.songs.size - 1) {
                currentSong = MainViewModel.songs[indexSong.get()]
                currentSong?.let { song ->
                    musicService?.mMediaPlayer = MediaPlayer.create(context, song.uri)
                    musicService?.mMediaPlayer?.start()
                    //  playSelectedSong(song.uri)
                    musicService?.startCustomForegroundService(song)
                }
            }
        }
    }

}