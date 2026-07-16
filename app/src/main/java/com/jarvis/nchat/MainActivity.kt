package com.jarvis.nchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
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
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
}