package com.jarvis.nchat.data.repository

import com.jarvis.nchat.core.datastore.TokenDataStore
import com.jarvis.nchat.core.network.ApiService
import com.jarvis.nchat.core.network.FcmTokenRequest
import com.jarvis.nchat.core.network.SocketManager
import com.jarvis.nchat.data.model.ChangePasswordRequest
import com.jarvis.nchat.data.model.ConfirmResetRequest
import com.jarvis.nchat.data.model.ForgotPasswordRequest
import com.jarvis.nchat.data.model.LoginRequest
import com.jarvis.nchat.data.model.RegisterRequest
import com.jarvis.nchat.data.model.VerifyOtpRequest
import com.jarvis.nchat.domain.model.User
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenDataStore: TokenDataStore,
    private val socketManager: SocketManager,
) {

    // ---- Register (2-step: request OTP, then verify) ----

    suspend fun registerRequest(username: String, email: String, password: String): Result<Unit> = runCatching {
        try {
            api.registerRequest(RegisterRequest(username, email, password))
            Unit
        } catch (e: HttpException) {
            throw Exception(e.parsedMessage("Registration failed"))
        }
    }

    suspend fun registerVerify(email: String, code: String): Result<User> = runCatching {
        try {
            val response = api.registerVerify(VerifyOtpRequest(email, code))
            tokenDataStore.saveSession(response.token, response.user.id)
            socketManager.connect(response.token)
            response.user.toDomain()
        } catch (e: HttpException) {
            throw Exception(e.parsedMessage("Verification failed"))
        }
    }

    // ---- Login ----

    suspend fun login(email: String, password: String): Result<User> = runCatching {
        try {
            val response = api.login(LoginRequest(email, password))
            tokenDataStore.saveSession(response.token, response.user.id)
            socketManager.connect(response.token)
            response.user.toDomain()
        } catch (e: HttpException) {
            throw Exception(e.parsedMessage("Login failed"))
        }
    }

    // ---- Forgot password (3-step: request OTP, verify OTP, confirm new password) ----

    suspend fun forgotPasswordRequest(email: String): Result<Unit> = runCatching {
        try {
            api.forgotPasswordRequest(ForgotPasswordRequest(email))
            Unit
        } catch (e: HttpException) {
            throw Exception(e.parsedMessage("Failed to send verification code"))
        }
    }

    suspend fun forgotPasswordVerify(email: String, code: String): Result<String> = runCatching {
        try {
            api.forgotPasswordVerify(VerifyOtpRequest(email, code)).resetToken
        } catch (e: HttpException) {
            throw Exception(e.parsedMessage("Invalid or expired code"))
        }
    }

    suspend fun forgotPasswordConfirm(resetToken: String, newPassword: String): Result<Unit> = runCatching {
        try {
            api.forgotPasswordConfirm(ConfirmResetRequest(resetToken, newPassword))
            Unit
        } catch (e: HttpException) {
            throw Exception(e.parsedMessage("Failed to reset password"))
        }
    }

    // ---- Change password while logged in ----

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        try {
            api.changePassword(ChangePasswordRequest(currentPassword, newPassword))
            Unit
        } catch (e: HttpException) {
            throw Exception(e.parsedMessage("Failed to change password"))
        }
    }

    // ---- Session / FCM ----

    suspend fun getCurrentUser(): Result<User> = runCatching {
        api.getMe().user.toDomain()
    }

    suspend fun isLoggedIn(): Boolean = tokenDataStore.token.first() != null

    suspend fun logout() {
        socketManager.disconnect()
        tokenDataStore.clearSession()
    }

    suspend fun getCurrentUserId(): String? = tokenDataStore.userId.first()

    suspend fun reconnectSocketIfLoggedIn() {
        tokenDataStore.token.first()?.let { socketManager.forceReconnect(it) }
    }

    suspend fun registerFcmToken(fcmToken: String): Result<Unit> = runCatching {
        api.registerFcmToken(FcmTokenRequest(fcmToken))
        Unit
    }
}

private fun HttpException.parsedMessage(default: String): String {
    val errorBody = response()?.errorBody()?.string()
    return try {
        org.json.JSONObject(errorBody ?: "").optString("error", default)
    } catch (e: Exception) {
        default
    }
}

private fun com.jarvis.nchat.data.model.UserDto.toDomain() = User(
    id = id,
    username = username,
    email = email,
    avatarUrl = avatar_url,
    isOnline = is_online ?: false,
)