package com.jarvis.nchat.presentation.calls

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IncomingCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        setContent {
            ChatAppTheme {
                IncomingCallHost(
                    fromUserId = intent.getStringExtra(EXTRA_FROM_USER_ID),
                    fromUsername = intent.getStringExtra(EXTRA_FROM_USERNAME),
                    conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID),
                    callId = intent.getStringExtra(EXTRA_CALL_ID),
                    onFinish = { finish() },
                )
            }
        }
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)
    }

    companion object {
        const val EXTRA_FROM_USER_ID = "from_user_id"
        const val EXTRA_FROM_USERNAME = "from_username"
        const val EXTRA_CONVERSATION_ID = "conversation_id"
        const val EXTRA_CALL_ID = "call_id"
    }
}

@Composable
private fun IncomingCallHost(
    fromUserId: String?,
    fromUsername: String?,
    conversationId: String?,
    callId: String?,
    onFinish: () -> Unit,
    viewModel: CallViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(fromUserId) {
        if (fromUserId != null && conversationId != null && uiState.status == CallStatus.IDLE) {
            viewModel.onIncomingCall(
                fromUserId = fromUserId,
                fromUsername = fromUsername ?: "Unknown",
                conversationId = conversationId,
                callId = callId,
            )
        }
    }

    when (uiState.status) {
        CallStatus.RINGING_INCOMING ->
            IncomingCallScreen(
                onCallAccepted = { },
                onCallDeclined = onFinish,
            )
        CallStatus.CONNECTING, CallStatus.CONNECTED ->
            ActiveCallScreen(onCallEnded = onFinish)
        CallStatus.ENDED -> onFinish()
        else -> { }
    }
}