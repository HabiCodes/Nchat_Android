package com.jarvis.nchat.core.navigation

sealed class Screen(val route: String) {
    object Chats : Screen("chats")
    object Search : Screen("search")
    object Calls : Screen("calls")
    object Profile : Screen("profile")

    object ChatDetail : Screen("chat_detail/{conversationId}?otherUserId={otherUserId}&username={username}&isOnline={isOnline}") {
        fun createRoute(conversationId: String, otherUserId: String, username: String, isOnline: Boolean) =
            "chat_detail/$conversationId?otherUserId=$otherUserId&username=$username&isOnline=$isOnline"
    }
    object UserProfile : Screen("user_profile/{userId}?username={username}") {
        fun createRoute(userId: String, username: String) = "user_profile/$userId?username=$username"
    }

    object IncomingCall : Screen("incoming_call/{callerId}") {
        fun createRoute(callerId: String) = "incoming_call/$callerId"
    }
    object ActiveCall : Screen("active_call/{conversationId}") {
        fun createRoute(conversationId: String) = "active_call/$conversationId"
    }

    object Settings : Screen("settings")
    object Login : Screen("login")
    object Register : Screen("register")
    object VerifyRegisterOtp : Screen("verify_register_otp/{email}") {
        fun createRoute(email: String) = "verify_register_otp/$email"
    }
    object ForgotPassword : Screen("forgot_password")
    object VerifyResetOtp : Screen("verify_reset_otp/{email}") {
        fun createRoute(email: String) = "verify_reset_otp/$email"
    }
    object ResetPassword : Screen("reset_password/{resetToken}") {
        fun createRoute(resetToken: String) = "reset_password/$resetToken"
    }
}