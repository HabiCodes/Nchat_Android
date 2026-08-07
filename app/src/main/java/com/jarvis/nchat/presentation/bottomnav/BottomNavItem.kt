package com.jarvis.nchat.presentation.bottomnav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.jarvis.nchat.core.navigation.Screen

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    companion object {
        val Chats = BottomNavItem(Screen.Chats.route, "Chats", Icons.Filled.ChatBubble)
        val Search = BottomNavItem(Screen.Search.route, "Search", Icons.Filled.Search)
        val Calls = BottomNavItem(Screen.Calls.route, "Calls", Icons.Filled.Call)
        val Profile = BottomNavItem(Screen.Profile.route, "Profile", Icons.Filled.Person)
    }
}