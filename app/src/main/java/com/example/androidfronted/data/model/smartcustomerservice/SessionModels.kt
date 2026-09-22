/**
 * 会话/消息接口数据模型（对齐 session.md，snake_case 响应）
 */
package com.example.androidfronted.data.model.smartcustomerservice

import com.google.gson.annotations.SerializedName

/** 会话对象（SessionItem） */
data class SessionItem(
    @SerializedName("session_id") val sessionId: String = "",
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("title") val title: String = "",
    @SerializedName("last_message_preview") val lastMessagePreview: String = "",
    @SerializedName("last_message_at") val lastMessageAt: String = "",
    @SerializedName("message_count") val messageCount: Int = 0,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("updated_at") val updatedAt: String = "",
    @SerializedName("pinned") val pinned: Boolean = false,
    @SerializedName("archived") val archived: Boolean = false
)

/** 消息对象（MessageItem） */
data class MessageItem(
    @SerializedName("message_id") val messageId: String = "",
    @SerializedName("session_id") val sessionId: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("role") val role: String = "user",
    @SerializedName("content") val content: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)

/** 分页包装（SessionListResponse / MessageListResponse 通用） */
data class SessionPage(
    val items: List<SessionItem> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false
)

data class MessagePage(
    val items: List<MessageItem> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false
)
