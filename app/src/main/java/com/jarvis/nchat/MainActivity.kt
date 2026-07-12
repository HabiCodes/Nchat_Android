package com.jarvis.nchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.navigation.ChatAppRoot
import com.jarvis.nchat.core.navigation.Screen
import com.jarvis.nchat.data.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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

            ChatAppTheme {
                startDestination?.let { ChatAppRoot(startDestination = it) }
            }
        }
    }
}