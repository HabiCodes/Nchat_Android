package com.jarvis.nchat.data.repository

import com.jarvis.nchat.core.datastore.TokenDataStore
import com.jarvis.nchat.core.network.ApiService
import com.jarvis.nchat.core.network.SocketManager
import com.jarvis.nchat.data.model.LoginRequest
import com.jarvis.nchat.data.model.RegisterRequest
import com.jarvis.nchat.domain.model.User
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import com.jarvis.nchat.data.model.ChangePasswordRequest

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenDataStore: TokenDataStore,
    private val socketManager: SocketManager,
) {
    suspend fun register(username: String, email: String, password: String): Result<User> = runCatching {
        try {
            val response = api.register(RegisterRequest(username, email, password))
            tokenDataStore.saveSession(response.token, response.user.id)
            socketManager.connect(response.token)
            response.user.toDomain()
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val message = try {
                org.json.JSONObject(errorBody ?: "").optString("error", "Registration failed")
            } catch (parseError: Exception) {
                "Registration failed"
            }
            throw Exception(message)
        }
    }

    suspend fun login(email: String, password: String): Result<User> = runCatching {
        val response = api.login(LoginRequest(email, password))
        tokenDataStore.saveSession(response.token, response.user.id)
        socketManager.connect(response.token)
        response.user.toDomain()
    }
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        try {
            api.changePassword(ChangePasswordRequest(currentPassword, newPassword))
            Unit
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val message = try { org.json.JSONObject(errorBody ?: "").optString("error", "Failed to change password") }
            catch (p: Exception) { "Failed to change password" }
            throw Exception(message)
        }
    }

    suspend fun getCurrentUser(): Result<User> = runCatching {
        api.getMe().user.toDomain()
    }

    suspend fun isLoggedIn(): Boolean = tokenDataStore.token.first() != null

    suspend fun logout() {
        socketManager.disconnect()
        tokenDataStore.clearSession()
    }

    suspend fun getCurrentUserId(): String? = tokenDataStore.userId.first()

    // Called once at app startup to resume the socket connection if a token already exists
    suspend fun reconnectSocketIfLoggedIn() {
        tokenDataStore.token.first()?.let { socketManager.connect(it) }
    }
}

private fun com.jarvis.nchat.data.model.UserDto.toDomain() = User(
    id = id,
    username = username,
    email = email,
    avatarUrl = avatar_url,
    isOnline = is_online ?: false,
)