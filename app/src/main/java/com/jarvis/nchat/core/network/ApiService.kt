package com.jarvis.nchat.core.network

import com.jarvis.nchat.data.model.*
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register/request")
    suspend fun registerRequest(@Body body: RegisterRequest): SimpleMessageResponse

    @POST("api/auth/register/verify")
    suspend fun registerVerify(@Body body: VerifyOtpRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): GenericSuccessResponse

    @GET("api/auth/me")
    suspend fun getMe(): MeResponse

    @POST("api/auth/password-reset/request")
    suspend fun forgotPasswordRequest(@Body body: ForgotPasswordRequest): SimpleMessageResponse

    @POST("api/auth/password-reset/verify")
    suspend fun forgotPasswordVerify(@Body body: VerifyOtpRequest): VerifyResetResponse

    @POST("api/auth/password-reset/confirm")
    suspend fun forgotPasswordConfirm(@Body body: ConfirmResetRequest): GenericSuccessResponse

    @POST("api/users/fcm-token")
    suspend fun registerFcmToken(@Body body: FcmTokenRequest): Map<String, Boolean>

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

    @GET("api/calls")
    suspend fun getCalls(): CallListResponse
}