package com.example.androidfronted.data.model.smartcustomerservice

import java.util.UUID

data class ChatRequest(
    val message: String,
    val sessionId: String? = null,  // 改为驼峰命名，匹配Java后端接口
    val agentMode: String = "react"  // 智能体范式，默认react
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),   // 本地渲染 ID
    val serverId: String? = null,                    // 后端 message_id
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

/** 会话 UI 模型（由 SessionItem 映射而来，id 即后端 session_id） */
data class ChatSession(
    val id: String,
    val title: String,
    val lastMessage: String = "",
    val timestamp: Long = 0L,             // updated_at 解析出的毫秒时间戳
    val messages: List<ChatMessage> = emptyList(),
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val messageCount: Int = 0
)
