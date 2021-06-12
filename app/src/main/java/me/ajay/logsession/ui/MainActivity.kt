package me.ajay.logsession.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import me.ajay.logsession.BROADCAST_INTENT_ACTION
import me.ajay.logsession.KEY_REMAINING_TIME
import me.ajay.logsession.R
import me.ajay.logsession.databinding.ActivityMainBinding
import me.ajay.logsession.service.SessionService

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val activityViewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartStop.setOnClickListener {
            activityViewModel.startStopSessionService()
        }

        lifecycleScope.launchWhenStarted {
            activityViewModel.sessionEvent.collect { event ->
                when (event) {
                    MainActivityViewModel.SessionEvent.StartSession -> {
                        startSession()
                    }
                    MainActivityViewModel.SessionEvent.EndSession -> {
                        endSession()
                    }
                    is MainActivityViewModel.SessionEvent.UpdateTimeReceived -> {
                        updateRemainingTime(event.remainingTime)
                    }
                }
            }
        }

    }

    private fun updateRemainingTime(remainingTime: String) {
        activityViewModel.isSessionActive.value = true
        binding.btnStartStop.text = getString(R.string.end_session)
        binding.txtTimeLeft.text = remainingTime
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            activityViewModel.onSessionTimeReceived(intent.getStringExtra(KEY_REMAINING_TIME)!!)
        }
    }

    private fun endSession() {
        val intent = Intent(this, SessionService::class.java)
        stopService(intent)
        binding.btnStartStop.text = getString(R.string.start_session)
        binding.txtTimeLeft.text = getString(R.string.default_time)
        activityViewModel.isSessionActive.value = false
    }

    private fun startSession() {
        val intent = Intent(this, SessionService::class.java)
        startService(intent)
        binding.btnStartStop.text = getString(R.string.end_session)
        activityViewModel.isSessionActive.value = true
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver, IntentFilter(BROADCAST_INTENT_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }
}