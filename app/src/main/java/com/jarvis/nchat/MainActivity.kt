package com.jarvis.nchat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.navigation.ChatAppRoot
import com.jarvis.nchat.core.navigation.Screen
import com.jarvis.nchat.data.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import  kotlinx.coroutines.launch
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.PowerManager
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied - either way, app continues normally.
           If denied, incoming-call and message pushes simply won't show
           a system notification, but the app keeps working otherwise. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        ensureFullScreenIntentPermission()
        setContent {
            var startDestination by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                val loggedIn = authRepository.isLoggedIn()
                if (loggedIn) authRepository.reconnectSocketIfLoggedIn()
                startDestination = if (loggedIn) Screen.Chats.route else Screen.Login.route
            }

            // Re-establish the socket every time the app comes back to the foreground -
            // Render's free tier drops connections on spin-down, so resuming from
            // background is the most common moment a stale connection needs refreshing.
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        kotlinx.coroutines.MainScope().launch {
                            if (authRepository.isLoggedIn()) authRepository.reconnectSocketIfLoggedIn()
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            ChatAppTheme {
                startDestination?.let { ChatAppRoot(startDestination = it) }
            }
        }
    }

    // Android 13+ requires explicitly asking for notification permission at
    // runtime - declaring it in the manifest alone does nothing. Without
    // this, NotificationManager.notify() silently no-ops: no crash, no
    // error, the notification just never appears.
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val alreadyGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!alreadyGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    private fun ensureFullScreenIntentPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        val nm = getSystemService(NotificationManager::class.java)
        if (!nm.canUseFullScreenIntent()) {
            startActivity(
                Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = Uri.parse("package:$packageName")
                }
            )
        }
    }
    private fun requestIgnoreBatteryOptimizations() {
        val pm = getSystemService(PowerManager::class.java)
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
            )
        }
    }
}