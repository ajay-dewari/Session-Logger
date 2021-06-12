package me.ajay.logsession.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import me.ajay.logsession.BROADCAST_INTENT_ACTION
import me.ajay.logsession.CHANNEL_ID
import me.ajay.logsession.FORTY_FIVE_MINUTES
import me.ajay.logsession.KEY_REMAINING_TIME
import me.ajay.logsession.NOTIFICATION_ID
import me.ajay.logsession.ONE_HOUR
import me.ajay.logsession.ONE_SECOND
import me.ajay.logsession.R
import me.ajay.logsession.THIRTY_MINUTES
import me.ajay.logsession.TWENTY_MINUTES
import java.util.*


class SessionService : Service() {

    var intent = Intent(BROADCAST_INTENT_ACTION)
    val sessions: List<Long> = listOf(TWENTY_MINUTES, THIRTY_MINUTES, FORTY_FIVE_MINUTES, ONE_HOUR)
    private var countDownTimer: CountDownTimer? = null
    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY;
    }

    override fun onCreate() {
        super.onCreate()
        countDownTimer = object : CountDownTimer(sessions.random(), ONE_SECOND) {
            @RequiresApi(api = Build.VERSION_CODES.O)
            override fun onTick(millisUntilFinished: Long) {
                val timeLeft = getFormattedTime(millisUntilFinished)
                createNotificationChannel()
                createUpdateNotification(timeLeft)
                intent.putExtra(KEY_REMAINING_TIME, timeLeft)
                sendBroadcast(intent)
            }
            override fun onFinish() {
                stopSelf()
            }
        }.start()
    }

    private fun getFormattedTime(timeLeftInMillis: Long): String {
        val minutes = (timeLeftInMillis / ONE_SECOND).toInt() / 60
        val seconds = (timeLeftInMillis / ONE_SECOND).toInt() % 60
        return java.lang.String.format(Locale.getDefault(), getString(R.string.time_format),
            minutes, seconds)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createUpdateNotification(timeLeft: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(timeLeft)
            .setContentText(getString(R.string.session_active))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(false).setOnlyAlertOnce(true)

        startForeground(NOTIFICATION_ID, builder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}