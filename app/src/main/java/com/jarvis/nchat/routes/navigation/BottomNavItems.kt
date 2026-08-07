package com.jarvis.nchat.routes.navigation

import com.jarvis.nchat.presentation.bottomnav.BottomNavItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import com.jarvis.nchat.core.navigation.Screen

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Chats.route,
        label = "Chats",
        icon = Icons.Default.ChatBubble
    ),
    BottomNavItem(
        route = Screen.Search.route,
        label = "Search",
        icon = Icons.Default.Search
    ),
    BottomNavItem(
        route = Screen.Calls.route,
        label = "Calls",
        icon = Icons.Default.Call
    ),
    BottomNavItem(
        route = Screen.Profile.route,
        label = "Profile",
        icon = Icons.Default.Person
    )
)