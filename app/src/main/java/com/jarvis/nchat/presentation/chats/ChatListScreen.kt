package com.jarvis.nchat.presentation.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.designsystem.Spacing
import com.jarvis.nchat.domain.model.Conversation
import com.jarvis.nchat.presentation.components.AvatarImage
import com.jarvis.nchat.presentation.components.OnlineStatusDot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatClick: (conversationId: String, otherUserId: String, username: String, isOnline: Boolean) -> Unit,
    onNewChatClick: () -> Unit,
    viewModel: ChatListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Chats", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewChatClick, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.Add, contentDescription = "New chat", tint = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Text(
                    "Couldn't load chats: ${uiState.error}",
                    modifier = Modifier.align(Alignment.Center).padding(Spacing.xl)
                )
                uiState.conversations.isEmpty() -> Text(
                    "No chats yet - search for someone to start one",
                    modifier = Modifier.align(Alignment.Center).padding(Spacing.xl),
                    color = ChatAppTheme.extendedColors.textSecondary
                )
                else -> LazyColumn(contentPadding = PaddingValues(vertical = Spacing.sm)) {
                    items(uiState.conversations, key = { it.id }) { conv ->
                        ChatRow(conv = conv, onClick = {
                            onChatClick(conv.id, conv.otherUserId, conv.otherUsername, conv.otherIsOnline)
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRow(conv: Conversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AvatarImage(name = conv.otherUsername, avatarUrl = conv.otherAvatarUrl, size = 52)
            if (conv.otherIsOnline) OnlineStatusDot(modifier = Modifier.align(Alignment.BottomEnd))
        }
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(conv.otherUsername, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                conv.lastMessage ?: "Say hello 👋",
                style = MaterialTheme.typography.bodyMedium,
                color = ChatAppTheme.extendedColors.textSecondary,
                maxLines = 1
            )
        }
    }
}