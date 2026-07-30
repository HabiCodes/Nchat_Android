package com.jarvis.nchat

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.jarvis.nchat.presentation.calls.CallSessionRepository
import com.jarvis.nchat.core.chat.MessageNotificationManager
import com.jarvis.nchat.core.chat.AppForegroundTracker

@HiltAndroidApp
class ChatApplication : Application() {

    @Inject lateinit var callSessionRepository: CallSessionRepository
    @Inject lateinit var messageNotificationManager: MessageNotificationManager
    @Inject lateinit var appForegroundTracker: AppForegroundTracker

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(appForegroundTracker)
        callSessionRepository.startListening()
        messageNotificationManager.startListening()
    }
}