package me.ajay.logsession.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.ajay.logsession.SESSION_SETTING
import javax.inject.Inject

class MainActivityViewModel @Inject constructor() : ViewModel() {

    private val eventChannel = Channel<SessionEvent>()
    val sessionEvent = eventChannel.receiveAsFlow()

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SESSION_SETTING)

    val isSessionActive = MutableLiveData(false)

     fun startStopSessionService() {
        viewModelScope.launch {
            if(isSessionActive.value == true){
                eventChannel.send(SessionEvent.EndSession)
            } else {
                eventChannel.send(SessionEvent.StartSession)
            }
        }
    }

     fun onSessionTimeReceived(remainingTime: String) {
        viewModelScope.launch {
            eventChannel.send(SessionEvent.UpdateTimeReceived(remainingTime))
        }
    }

    sealed class SessionEvent {
        object StartSession : SessionEvent()
        object EndSession : SessionEvent()
        data class UpdateTimeReceived(val remainingTime: String) : SessionEvent()
    }

}