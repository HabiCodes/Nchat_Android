package com.jarvis.nchat.data.repository

import com.jarvis.nchat.core.network.ApiService
import com.jarvis.nchat.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: ApiService,
) {
    suspend fun searchUsers(query: String): Result<List<User>> = runCatching {
        if (query.trim().length < 2) return@runCatching emptyList()
        api.searchUsers(query).users.map {
            User(id = it.id, username = it.username, email = it.email, avatarUrl = it.avatar_url, isOnline = it.is_online ?: false)
        }
    }
}