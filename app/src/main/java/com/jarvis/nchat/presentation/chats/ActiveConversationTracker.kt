package com.jarvis.nchat.core.chat

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveConversationTracker @Inject constructor() {
    @Volatile var activeConversationId: String? = null
}