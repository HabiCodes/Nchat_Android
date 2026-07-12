package com.jarvis.nchat.domain.model

data class User(
    val id: String,
    val username: String,
    val email: String?,
    val avatarUrl: String?,
    val isOnline: Boolean,
)