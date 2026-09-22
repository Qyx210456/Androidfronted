/**
 * 聊天数据仓库（远端权威）
 *
 * 会话与消息一律以后端 session.md 接口为准：
 * - 会话列表/新建/重命名/置顶/删除 → SessionApiService
 * - 历史消息分页 → GET /api/sessions/{id}/messages（cursor 向上翻页）
 * - 发送消息 → POST /api/chat（SSE），sessionId 使用后端会话 ID
 *
 * 打字机效果由 ViewModel 层的逐字缓冲实现，本仓库只负责完整 chunk 回调。
 */
package com.example.androidfronted.data.repository

import android.content.Context
import android.util.Log
import com.example.androidfronted.data.model.smartcustomerservice.ChatMessage
import com.example.androidfronted.data.model.smartcustomerservice.ChatRequest
import com.example.androidfronted.data.model.smartcustomerservice.ChatSession
import com.example.androidfronted.data.model.smartcustomerservice.MessagePage
import com.example.androidfronted.data.model.smartcustomerservice.SessionItem
import com.example.androidfronted.data.model.smartcustomerservice.SessionPage
import com.example.androidfronted.data.source.SessionApiService
import com.example.androidfronted.network.ChatSseClient
import java.text.SimpleDateFormat
import java.util.Locale

class ChatRepository private constructor(private val context: Context) {

    companion object {
        private const val TAG = "ChatRepository"

        @Volatile
        private var instance: ChatRepository? = null

        fun getInstance(context: Context): ChatRepository {
            return instance ?: synchronized(this) {
                instance ?: ChatRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val api = SessionApiService(context)
    private val sseClient = ChatSseClient(context)

    private var currentSessionId: String? = null

    /** 会话列表分页游标（由 loadSessions 维护） */
    var sessionsNextCursor: String? = null
        private set
    var sessionsHasMore: Boolean = false
        private set

    fun getCurrentSessionId(): String? = currentSessionId
    fun setCurrentSessionId(sessionId: String?) {
        currentSessionId = sessionId
    }

    // ---------- 时间解析：yyyy-MM-dd HH:mm:ss → Long ----------
    private fun parseServerTime(time: String?): Long {
        if (time.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.parse(time)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun SessionItem.toChatSession(): ChatSession = ChatSession(
        id = sessionId,
        title = title,
        lastMessage = lastMessagePreview,
        timestamp = parseServerTime(updatedAt.ifBlank { lastMessageAt.ifBlank { createdAt } }),
        pinned = pinned,
        archived = archived,
        messageCount = messageCount
    )

    // ---------- 会话列表 ----------
    /**
     * 拉取会话列表。
     * @param cursor 为 null 时刷新首页；传 nextCursor 加载下一页
     * @param onResult (列表, nextCursor, hasMore, 失败消息) 失败消息为 null 表示成功
     */
    fun loadSessions(
        cursor: String? = null,
        limit: Int = 20,
        onResult: (List<ChatSession>, String?, Boolean, String?) -> Unit
    ) {
        api.getSessions(
            cursor = cursor,
            limit = limit,
            includeArchived = false,
            hasMessages = false,
            callback = object : SessionApiService.ApiCallback<SessionPage> {
            override fun onSuccess(result: SessionPage) {
                sessionsNextCursor = result.nextCursor
                sessionsHasMore = result.hasMore
                onResult(result.items.map { it.toChatSession() }, result.nextCursor, result.hasMore, null)
            }

            override fun onError(message: String, code: Int) {
                Log.e(TAG, "加载会话列表失败: $message ($code)")
                onResult(emptyList(), null, false, message)
            }
        })
    }

    /** 获取最近一次有消息的会话（进入上次对话场景：limit=1&has_messages=true） */
    fun getLatestSessionWithMessages(onResult: (ChatSession?, String?) -> Unit) {
        api.getSessions(
            cursor = null,
            limit = 1,
            includeArchived = false,
            hasMessages = true,
            callback = object : SessionApiService.ApiCallback<SessionPage> {
            override fun onSuccess(result: SessionPage) {
                onResult(result.items.firstOrNull()?.toChatSession(), null)
            }

            override fun onError(message: String, code: Int) {
                onResult(null, message)
            }
        })
    }

    // ---------- 新建会话 ----------
    fun createSession(onResult: (ChatSession?, String?) -> Unit) {
        api.createSession(object : SessionApiService.ApiCallback<SessionItem> {
            override fun onSuccess(result: SessionItem) {
                val session = result.toChatSession()
                currentSessionId = session.id
                onResult(session, null)
            }

            override fun onError(message: String, code: Int) {
                onResult(null, message)
            }
        })
    }

    // ---------- 重命名 ----------
    fun renameSession(sessionId: String, title: String, onResult: (ChatSession?, String?) -> Unit) {
        api.updateSession(
            sessionId,
            title = title,
            pinned = null,
            archived = null,
            callback = object : SessionApiService.ApiCallback<SessionItem> {
            override fun onSuccess(result: SessionItem) = onResult(result.toChatSession(), null)
            override fun onError(message: String, code: Int) = onResult(null, message)
        })
    }

    // ---------- 置顶/取消置顶 ----------
    fun setPinned(sessionId: String, pinned: Boolean, onResult: (ChatSession?, String?) -> Unit) {
        api.pinSession(sessionId, pinned, object : SessionApiService.ApiCallback<SessionItem> {
            override fun onSuccess(result: SessionItem) = onResult(result.toChatSession(), null)
            override fun onError(message: String, code: Int) = onResult(null, message)
        })
    }

    /** 批量置顶（多选模式），逐个调用 */
    fun setPinnedBatch(sessionIds: List<String>, pinned: Boolean, onResult: (Int, String?) -> Unit) {
        if (sessionIds.isEmpty()) {
            onResult(0, null)
            return
        }
        var success = 0
        var error: String? = null
        var settled = 0
        sessionIds.forEach { id ->
            api.pinSession(id, pinned, object : SessionApiService.ApiCallback<SessionItem> {
                override fun onSuccess(result: SessionItem) {
                    success++
                    settled++
                    if (settled == sessionIds.size) onResult(success, error)
                }

                override fun onError(message: String, code: Int) {
                    if (error == null) error = message
                    settled++
                    if (settled == sessionIds.size) onResult(success, error)
                }
            })
        }
    }

    // ---------- 删除 ----------
    fun deleteSession(sessionId: String, onResult: (String?, String?) -> Unit) {
        api.deleteSession(sessionId, object : SessionApiService.ApiCallback<String> {
            override fun onSuccess(result: String) {
                if (currentSessionId == sessionId) {
                    currentSessionId = null
                }
                onResult(result, null)
            }

            override fun onError(message: String, code: Int) = onResult(null, message)
        })
    }

    /** 批量删除（多选模式），逐个调用 */
    fun deleteSessionBatch(sessionIds: List<String>, onResult: (Int, String?) -> Unit) {
        if (sessionIds.isEmpty()) {
            onResult(0, null)
            return
        }
        var success = 0
        var error: String? = null
        var settled = 0
        sessionIds.forEach { id ->
            api.deleteSession(id, object : SessionApiService.ApiCallback<String> {
                override fun onSuccess(result: String) {
                    if (currentSessionId == id) currentSessionId = null
                    success++
                    settled++
                    if (settled == sessionIds.size) onResult(success, error)
                }

                override fun onError(message: String, code: Int) {
                    if (error == null) error = message
                    settled++
                    if (settled == sessionIds.size) onResult(success, error)
                }
            })
        }
    }

    // ---------- 历史消息分页 ----------
    /**
     * 拉取会话消息。
     * @param cursor 为 null 时取最新一页（首屏）；传 nextCursor 向上加载更早
     * @param onResult (消息列表按时间升序, nextCursor, hasMore, 失败消息)
     */
    fun fetchMessages(
        sessionId: String,
        cursor: String? = null,
        limit: Int = 30,
        onResult: (List<ChatMessage>, String?, Boolean, String?) -> Unit
    ) {
        api.getMessages(sessionId, cursor, limit, object : SessionApiService.ApiCallback<MessagePage> {
            override fun onSuccess(result: MessagePage) {
                // 后端同轮 user/assistant 消息同一秒落库（created_at 相同）且查询无稳定二级排序，
                // 可能返回 b b a 错序；同一时间戳内强制 user 在 assistant 前，恢复一问一答顺序
                val messages = result.items.map { item ->
                    ChatMessage(
                        id = if (item.messageId.isBlank()) java.util.UUID.randomUUID().toString() else item.messageId,
                        serverId = item.messageId.ifBlank { null },
                        content = item.content,
                        isFromUser = item.role == "user",
                        timestamp = parseServerTime(item.createdAt)
                    )
                }.sortedWith(compareBy({ it.timestamp }, { if (it.isFromUser) 0 else 1 }))
                onResult(messages, result.nextCursor, result.hasMore, null)
            }

            override fun onError(message: String, code: Int) {
                onResult(emptyList(), null, false, message)
            }
        })
    }

    // ---------- 发送消息（SSE） ----------
    fun sendMessage(
        message: String,
        sessionId: String?,
        onSessionInit: (String) -> Unit,
        onMessageChunk: (String) -> Unit,
        onError: (String) -> Unit,
        onComplete: (String) -> Unit   // 回传 AI 完整回复文本
    ) {
        if (sessionId != null) {
            currentSessionId = sessionId
        }
        val request = ChatRequest(
            message = message,
            sessionId = currentSessionId
        )

        val fullTextBuilder = StringBuilder()

        sseClient.streamChat(
            request = request,
            onSessionInit = { sid ->
                currentSessionId = sid
                onSessionInit(sid)
            },
            onMessage = { chunk ->
                fullTextBuilder.append(chunk)
                onMessageChunk(chunk)
            },
            onToolCall = { },
            onToolResult = { },
            onError = { error ->
                onError(error)
            },
            onComplete = {
                onComplete(fullTextBuilder.toString())
            }
        )
    }

    fun cancelCurrentRequest() {
        sseClient.cancel()
    }
}
