package com.jarvis.nchat

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.jarvis.nchat.presentation.calls.CallSessionRepository

@HiltAndroidApp
class ChatApplication : Application() {

    @Inject lateinit var callSessionRepository: CallSessionRepository

    override fun onCreate() {
        super.onCreate()
        callSessionRepository.startListening()
    }
}