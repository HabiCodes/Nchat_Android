package com.jarvis.nchat.core.network

import com.jarvis.nchat.core.datastore.TokenDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenDataStore: TokenDataStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Skip attaching a token on register/login - they don't need auth and won't have one yet
        val request = chain.request()
        val isAuthFreeRoute = request.url.encodedPath.let {
            it.endsWith("/api/auth/register") || it.endsWith("/api/auth/login")
        }
        if (isAuthFreeRoute) return chain.proceed(request)

        val token = runBlocking { tokenDataStore.token.first() }
        val newRequest = if (token != null) {
            request.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else request

        return chain.proceed(newRequest)
    }
}