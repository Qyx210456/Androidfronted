package com.example.androidfronted.data.repository

import android.content.Context
import android.util.Log
import com.example.androidfronted.data.model.smartcustomerservice.ChatMessage
import com.example.androidfronted.data.model.smartcustomerservice.ChatRequest
import com.example.androidfronted.data.model.smartcustomerservice.ChatSession
import com.example.androidfronted.network.ChatSseClient

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
    
    private val sseClient = ChatSseClient(context)
    private val sessions = mutableListOf<ChatSession>()
    private var currentSessionId: String? = null
    private var welcomeMessage: String = ""
    
    fun getWelcomeMessage(): String = welcomeMessage
    
    fun clearWelcomeMessage() {
        welcomeMessage = ""
    }
    
    fun initSession(
        onSessionInit: (String) -> Unit,
        onMessageChunk: (String) -> Unit,
        onError: (String) -> Unit,
        onComplete: () -> Unit
    ) {
        val welcomeBuilder = StringBuilder()
        
        sseClient.initSession(
            onSessionInit = { sessionId ->
                currentSessionId = sessionId
                onSessionInit(sessionId)
            },
            onMessage = { chunk ->
                welcomeBuilder.append(chunk)
                onMessageChunk(chunk)
            },
            onError = { error ->
                onError(error)
            },
            onComplete = {
                welcomeMessage = welcomeBuilder.toString()
                onComplete()
            }
        )
    }
    
    fun getCurrentSession(): ChatSession? {
        return sessions.find { it.id == currentSessionId }
    }
    
    fun getAllSessions(): List<ChatSession> {
        return sessions.toList()
    }
    
    fun createNewSession(): ChatSession {
        val session = ChatSession(
            title = "新对话",
            timestamp = System.currentTimeMillis()
        )
        sessions.add(0, session)
        currentSessionId = session.id
        Log.d(TAG, "Created new session: ${session.id}")
        return session
    }
    
    fun switchSession(sessionId: String): ChatSession? {
        val session = sessions.find { it.id == sessionId }
        if (session != null) {
            currentSessionId = sessionId
            Log.d(TAG, "Switched to session: $sessionId")
        }
        return session
    }
    
    fun deleteSession(sessionId: String) {
        sessions.removeAll { it.id == sessionId }
        if (currentSessionId == sessionId) {
            currentSessionId = sessions.firstOrNull()?.id
        }
        Log.d(TAG, "Deleted session: $sessionId")
    }
    
    fun sendMessage(
        message: String,
        currentMessages: List<ChatMessage>,
        onSessionInit: (String) -> Unit,
        onMessageChunk: (String) -> Unit,
        onToolCall: (String, Map<String, Any>) -> Unit,
        onError: (String) -> Unit,
        onComplete: (List<ChatMessage>) -> Unit
    ) {
        var session = getCurrentSession()
        if (session == null) {
            session = createNewSession()
        }
        
        val messagesWithUser = currentMessages
        
        updateSessionMessages(session.id, messagesWithUser)
        
        val request = ChatRequest(
            message = message,
            sessionId = currentSessionId  // 改为驼峰命名
        )
        
        val aiMessageBuilder = StringBuilder()
        val aiMessageId = java.util.UUID.randomUUID().toString()
        
        sseClient.streamChat(
            request = request,
            onSessionInit = { sessionId ->
                currentSessionId = sessionId
                onSessionInit(sessionId)
            },
            onMessage = { chunk ->
                Log.d(TAG, "收到chunk: '$chunk', 长度: ${chunk.length}, 包含换行: ${chunk.contains("\n")}")
                aiMessageBuilder.append(chunk)
                onMessageChunk(chunk)
            },
            onToolCall = { toolCallInfo ->
                onToolCall(toolCallInfo.toolName, toolCallInfo.arguments)
            },
            onToolResult = { result ->
                Log.d(TAG, "Tool result: $result")
            },
            onError = { error ->
                onError(error)
            },
            onComplete = {
                val aiMessage = ChatMessage(
                    id = aiMessageId,
                    content = aiMessageBuilder.toString(),
                    isFromUser = false
                )
                
                val finalMessages = messagesWithUser + aiMessage
                
                val currentSession = getCurrentSession()
                if (currentSession != null) {
                    updateSessionMessages(currentSession.id, finalMessages)
                    
                    if (currentSession.title == "新对话") {
                        updateSessionTitle(currentSession.id, message.take(20))
                    }
                }
                
                onComplete(finalMessages)
            }
        )
    }
    
    fun cancelCurrentRequest() {
        sseClient.cancel()
        Log.d(TAG, "Cancelled current request")
    }
    
    private fun updateSessionMessages(sessionId: String, messages: List<ChatMessage>) {
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index >= 0) {
            sessions[index] = sessions[index].copy(
                messages = messages,
                lastMessage = messages.lastOrNull()?.content ?: "",
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    private fun updateSessionTitle(sessionId: String, title: String) {
        val index = sessions.indexOfFirst { it.id == sessionId }
        if (index >= 0) {
            sessions[index] = sessions[index].copy(title = title)
        }
    }
}
