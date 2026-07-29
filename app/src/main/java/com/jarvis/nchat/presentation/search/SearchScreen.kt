package com.jarvis.nchat.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.nchat.core.designsystem.ChatAppTheme
import com.jarvis.nchat.core.designsystem.Spacing
import com.jarvis.nchat.domain.model.User
import com.jarvis.nchat.presentation.components.AvatarImage
import com.jarvis.nchat.presentation.components.OnlineStatusDot
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onUserClick: (String, String) -> Unit,
    onStartChatClick: (String, String, String) -> Unit, // conversationId, otherUserId, username
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Search", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
                placeholder = { Text("Search by username") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                )
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (uiState.query.isBlank()) {
                EmptySearchState()
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = Spacing.lg)) {
                    items(uiState.results, key = { it.id }) { user ->
                        SearchResultRow(
                            user = user,
                            onUserClick = onUserClick,
                            onChatClick = {
                                scope.launch {
                                    viewModel.startChatWith(user.id)?.let { conversationId ->
                                        onStartChatClick(conversationId, user.id, user.username)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.PersonSearch,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = ChatAppTheme.extendedColors.textSecondary
        )
        Spacer(Modifier.height(Spacing.md))
        Text(
            "Find people by their username",
            style = MaterialTheme.typography.bodyMedium,
            color = ChatAppTheme.extendedColors.textSecondary
        )
    }
}

@Composable
private fun SearchResultRow(
    user: User,
    onUserClick: (String, String) -> Unit,
    onChatClick: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick(user.id, user.username) }
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Box {

            AvatarImage(name = user.username, avatarUrl = user.avatarUrl, size = 48)
            if (user.isOnline) OnlineStatusDot(modifier = Modifier.align(Alignment.BottomEnd))
        }
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                if (user.isOnline) "Online" else "Offline",
                style = MaterialTheme.typography.bodySmall,
                color = ChatAppTheme.extendedColors.textSecondary
            )
        }
        Button(onClick = onChatClick, shape = MaterialTheme.shapes.extraLarge) {
            Text("Chat")
        }
    }
}