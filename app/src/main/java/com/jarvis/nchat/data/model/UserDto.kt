package com.jarvis.nchat.data.model

data class UserDto(
    val id: String,
    val username: String,
    val email: String?,
    val avatar_url: String?,
    val is_online: Boolean? = null,
    val last_seen_at: String? = null,
    val created_at: String? = null,
)

data class AuthResponse(
    val user: UserDto,
    val token: String,
)

data class RegisterRequest(val username: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class UserSearchResponse(val users: List<UserDto>)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)