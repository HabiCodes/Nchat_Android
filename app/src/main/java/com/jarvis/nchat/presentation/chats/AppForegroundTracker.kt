package com.jarvis.nchat.core.chat

import android.app.Activity
import android.app.Application
import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppForegroundTracker @Inject constructor() : Application.ActivityLifecycleCallbacks {
    @Volatile private var startedCount = 0
    val isForeground: Boolean get() = startedCount > 0

    override fun onActivityStarted(activity: Activity) { startedCount++ }
    override fun onActivityStopped(activity: Activity) { startedCount-- }
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}