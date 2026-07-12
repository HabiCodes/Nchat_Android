package com.jarvis.nchat.core.network

import com.jarvis.nchat.data.model.*
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("api/auth/me")
    suspend fun getMe(): AuthResponse

    @GET("api/users/search")
    suspend fun searchUsers(@Query("q") query: String): UserSearchResponse

    @GET("api/conversations")
    suspend fun getConversations(): ConversationListResponse

    @POST("api/conversations")
    suspend fun startConversation(@Body body: StartConversationRequest): StartConversationResponse

    @GET("api/conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("before") before: String? = null,
        @Query("limit") limit: Int = 50,
    ): MessageListResponse
}