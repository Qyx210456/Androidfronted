package com.example.androidfronted.data.model.smartcustomerservice

import java.util.UUID

data class ChatRequest(
    val message: String,
    val session_id: String? = null
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val toolCall: ToolCallInfo? = null
)

data class ToolCallInfo(
    val toolName: String,
    val arguments: Map<String, Any>
)

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val lastMessage: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val messages: List<ChatMessage> = emptyList()
)
