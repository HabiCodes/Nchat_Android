package com.jarvis.nchat.core.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jarvis.nchat.presentation.auth.LoginScreen
import com.jarvis.nchat.presentation.auth.RegisterScreen
import com.jarvis.nchat.presentation.calls.CallHistoryScreen
import com.jarvis.nchat.presentation.chats.ChatDetailScreen
import com.jarvis.nchat.presentation.chats.ChatListScreen
import com.jarvis.nchat.presentation.profile.ProfileScreen
import com.jarvis.nchat.presentation.search.SearchScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.currentBackStackEntryAsState
@Composable
fun ChatAppRoot(startDestination: String) {
    val navController = rememberNavController()

    // Only show bottom nav on the 4 main tabs, not on auth/chat-detail screens
    val backStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            val currentRoute = backStackEntry?.destination?.route
            val tabRoutes = setOf(Screen.Chats.route, Screen.Search.route, Screen.Calls.route, Screen.Profile.route)
            if (currentRoute in tabRoutes) ChatAppBottomNavBar(navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { navController.navigate(Screen.Chats.route) { popUpTo(0) } },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { navController.navigate(Screen.Chats.route) { popUpTo(0) } },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                )
            }
            composable(Screen.Chats.route) {
                ChatListScreen(
                    onChatClick = { conversationId -> navController.navigate(Screen.ChatDetail.createRoute(conversationId)) },
                    onNewChatClick = { navController.navigate(Screen.Search.route) }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onStartChatClick = { conversationId -> navController.navigate(Screen.ChatDetail.createRoute(conversationId)) }
                )
            }
            composable(Screen.Calls.route) {
                CallHistoryScreen(onCallEntryClick = {}, onAudioCallClick = {}, onVideoCallClick = {})
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    username = "Habishek", email = "", avatarUrl = null,
                    onSettingsClick = {}, onLogoutClick = {}
                )
            }
            composable(Screen.ChatDetail.route) {
                ChatDetailScreen(
                    contactName = "Chat",
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

