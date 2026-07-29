package com.jarvis.nchat.core.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jarvis.nchat.presentation.auth.ForgotPasswordScreen
import com.jarvis.nchat.presentation.auth.LoginScreen
import com.jarvis.nchat.presentation.auth.RegisterScreen
import com.jarvis.nchat.presentation.auth.RegisterVerifyScreen
import com.jarvis.nchat.presentation.auth.ResetPasswordScreen
import com.jarvis.nchat.presentation.auth.ResetVerifyScreen
import com.jarvis.nchat.presentation.calls.ActiveCallScreen
import com.jarvis.nchat.presentation.calls.CallHistoryScreen
import com.jarvis.nchat.presentation.calls.CallStatus
import com.jarvis.nchat.presentation.calls.CallViewModel
import com.jarvis.nchat.presentation.calls.IncomingCallScreen
import com.jarvis.nchat.presentation.chats.ChatDetailScreen
import com.jarvis.nchat.presentation.chats.ChatListScreen
import com.jarvis.nchat.presentation.profile.ProfileScreen
import com.jarvis.nchat.presentation.search.SearchScreen
import com.jarvis.nchat.presentation.profile.UserProfileScreen

@Composable
fun ChatAppRoot(startDestination: String) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    val activity = LocalContext.current as ComponentActivity
    val callViewModel: CallViewModel = hiltViewModel(activity)
    val callState by callViewModel.uiState.collectAsState()

    LaunchedEffect(callState.status) {
        if (callState.status == CallStatus.RINGING_INCOMING) {
            navController.navigate(Screen.IncomingCall.route)
        }
    }

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
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onOtpSent = { email -> navController.navigate(Screen.VerifyRegisterOtp.createRoute(email)) },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                )
            }
            composable(Screen.VerifyRegisterOtp.route) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                RegisterVerifyScreen(
                    email = email,
                    onVerified = { navController.navigate(Screen.Chats.route) { popUpTo(0) } },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onOtpSent = { email -> navController.navigate(Screen.VerifyResetOtp.createRoute(email)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.VerifyResetOtp.route) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                ResetVerifyScreen(
                    email = email,
                    onVerified = { resetToken ->
                        navController.navigate(Screen.ResetPassword.createRoute(resetToken))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ResetPassword.route) { backStackEntry ->
                val resetToken = backStackEntry.arguments?.getString("resetToken") ?: ""
                ResetPasswordScreen(
                    resetToken = resetToken,
                    onDone = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Chats.route) {
                ChatListScreen(
                    onChatClick = { conversationId, otherUserId, username, isOnline ->
                        navController.navigate(Screen.ChatDetail.createRoute(conversationId, otherUserId, username, isOnline))
                    },
                    onNewChatClick = { navController.navigate(Screen.Search.route) }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onStartChatClick = { conversationId, otherUserId, username ->
                        navController.navigate(
                            Screen.ChatDetail.createRoute(conversationId, otherUserId, username, false)
                        )
                    },
                    onUserClick = { userId, username ->
                        navController.navigate(Screen.UserProfile.createRoute(userId, username))
                    }
                )
            }
            composable(Screen.Calls.route) {
                CallHistoryScreen(
                    onAudioCallClick = { userId, username ->
                        navController.navigate(
                            Screen.ActiveCall.createRoute(userId)
                        )
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLoggedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable(Screen.UserProfile.route) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: "User"
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                UserProfileScreen(
                    username = username,
                    avatarUrl = null,
                    isOnline = false, // TODO: pass real online status through nav args like ChatDetail does
                    onBackClick = { navController.popBackStack() },
                    onMessageClick = {
                        navController.navigate(Screen.ChatDetail.createRoute("", userId, username, false))
                    },
                    onAudioCallClick = { navController.navigate(Screen.ActiveCall.createRoute(userId)) },
                    onVideoCallClick = { navController.navigate(Screen.ActiveCall.createRoute(userId)) }
                )
            }

            composable(Screen.ChatDetail.route) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
                val username = backStackEntry.arguments?.getString("username") ?: "Chat"
                val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
                val isOnline = backStackEntry.arguments?.getString("isOnline")?.toBoolean() ?: false
                ChatDetailScreen(
                    contactName = username,
                    otherUserId = otherUserId,
                    conversationId = conversationId,
                    isOnline = isOnline,
                    onBackClick = { navController.popBackStack() },
                    onStartCall = { navController.navigate(Screen.ActiveCall.createRoute(otherUserId)) }
                )
            }
            composable(Screen.IncomingCall.route) {
                IncomingCallScreen(
                    onCallAccepted = {
                        navController.navigate(Screen.ActiveCall.createRoute("")) {
                            popUpTo(Screen.IncomingCall.route) { inclusive = true }
                        }
                    },
                    onCallDeclined = { navController.popBackStack() }
                )
            }
            composable(Screen.ActiveCall.route) {
                ActiveCallScreen(
                    onCallEnded = { navController.popBackStack(Screen.Chats.route, inclusive = false) }
                )
            }
        }
    }
}