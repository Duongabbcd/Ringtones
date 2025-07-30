package com.ezt.ringify.ringtonewallpaper.screen.ringtone.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ezt.ringify.ringtonewallpaper.R
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player

class RingtonePlayerService : Service() {

    private val binder = LocalBinder()
    private lateinit var player: ExoPlayer
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L // 1 second

    inner class LocalBinder : Binder() {
        fun getService() = this@RingtonePlayerService
    }

    var currentPlayingId: Int = -1
        private set

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> resume()
            ACTION_PAUSE -> pause()
            ACTION_STOP -> {
                stop()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()

        createNotificationChannel()
        startForeground(1, createNotification("Ready to play"))

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    handler.post(progressUpdater)
                } else {
                    handler.removeCallbacks(progressUpdater)
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    broadcastSongEnded()
                }
            }
        })
    }

    private val progressUpdater = object : Runnable {
        override fun run() {
            if (player.isPlaying) {
                val progress = player.currentPosition
                broadcastProgress(progress)
                handler.postDelayed(this, updateInterval)
            }
        }
    }

    fun playUri(uriString: String, ringtoneId: Int) {
        println("Service playUri called with: $uriString and $ringtoneId")
        currentPlayingId = ringtoneId
        println("Player state before stop: ${player.playbackState}")
        player.stop()
        println("Player stopped")
        player.setMediaItem(MediaItem.fromUri(uriString))
        println("MediaItem set")
        player.prepare()
        println("Player prepared")
        player.play()
        println("Player play() called")
        updateNotification("Playing ringtone")
    }

    fun isAtEnd(): Boolean {
        return player.playbackState == Player.STATE_ENDED
    }

    fun stop() {
        println("RingtonePlayerService: stop() called")
        player.stop()
        player.clearMediaItems()
        player.seekTo(0)
        currentPlayingId = -10

    }


    fun pause() {
        player.pause()
        updateNotification("Paused")
        broadcastPlaybackStateChanged("Paused")
    }

    fun resume() {
        player.play()
        updateNotification("Playing ringtone")
        broadcastPlaybackStateChanged("Playing ringtone")
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun isPlaying() = player.isPlaying

    fun getCurrentPosition() = player.currentPosition

    fun release() {
        handler.removeCallbacks(progressUpdater)
        player.release()
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onDestroy() {
        super.onDestroy()
        release()
    }

    private fun broadcastPlaybackStateChanged(action: String) {
        val intent = Intent("com.ezt.ringify.PLAYBACK_ACTION")
        intent.putExtra("action", action)
        intent.putExtra("ringtoneId", currentPlayingId)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastProgress(progressMs: Long) {
        val intent = Intent("ringtone_progress")
        intent.putExtra("progress", progressMs)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastSongEnded() {
        val intent = Intent("ringtone_ended")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, "ringtone_channel")
            .setContentTitle("Ringtone Player")
            .setContentText(contentText)
            .setSmallIcon(if(contentText in listOf("Pause", "Stop")){R.drawable.icon_play} else R.drawable.icon_pause) // Your app icon here
            .build()
    }

    private fun updateNotification(contentText: String) {
        val playIntent = Intent(this, RingtonePlayerService::class.java).apply {
            action = ACTION_PLAY
        }
        val pauseIntent = Intent(this, RingtonePlayerService::class.java).apply {
            action = ACTION_PAUSE
        }
        val stopIntent = Intent(this, RingtonePlayerService::class.java).apply {
            action = ACTION_STOP
        }

        val playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stopPendingIntent = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "ringtone_channel")
            .setContentTitle("Ringtone Player")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.icon_ringtone)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        startForeground(1, builder.build())
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ringtone_channel",
                "Ringtone Player Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_PLAY = "com.ezt.ringify.ACTION_PLAY"
        const val ACTION_PAUSE = "com.ezt.ringify.ACTION_PAUSE"
        const val ACTION_STOP = "com.ezt.ringify.ACTION_STOP"
    }

}
