package com.jarvis.nchat.data.model

data class MeResponse(val user: UserDto)
data class SimpleMessageResponse(val message: String)

data class VerifyOtpRequest(val email: String, val code: String)

data class ForgotPasswordRequest(val email: String)
data class VerifyResetResponse(val message: String, val resetToken: String)
data class ConfirmResetRequest(val resetToken: String, val newPassword: String)
data class GenericSuccessResponse(val success: Boolean, val message: String? = null)