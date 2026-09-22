package com.example.androidfronted.viewmodel.smartcustomerservice

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfronted.data.model.UserInfoResponse
import com.example.androidfronted.data.model.smartcustomerservice.ChatMessage
import com.example.androidfronted.data.model.smartcustomerservice.ChatSession
import com.example.androidfronted.data.repository.ChatRepository
import com.example.androidfronted.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ChatViewModel"
        /** 打字机逐字间隔（毫秒） */
        private const val TYPEWRITER_DELAY_MS = 18L
    }

    private val repository = ChatRepository.getInstance(application)
    private val userRepository = UserRepository.getInstance(application)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions.asStateFlow()

    private val _currentStreamingMessage = MutableStateFlow<String>("")
    val currentStreamingMessage: StateFlow<String> = _currentStreamingMessage.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _userAvatar = MutableStateFlow<String?>(null)
    val userAvatar: StateFlow<String?> = _userAvatar.asStateFlow()

    private val _welcomeMessage = MutableStateFlow<String>("")
    val welcomeMessage: StateFlow<String> = _welcomeMessage.asStateFlow()

    private val _isWelcomeStreaming = MutableStateFlow(false)
    val isWelcomeStreaming: StateFlow<Boolean> = _isWelcomeStreaming.asStateFlow()

    private val _showWelcome = MutableStateFlow(true)
    val showWelcome: StateFlow<Boolean> = _showWelcome.asStateFlow()

    /** 历史消息向上分页状态 */
    private val _isLoadingOlder = MutableStateFlow(false)
    val isLoadingOlder: StateFlow<Boolean> = _isLoadingOlder.asStateFlow()

    private val _isLoadingSessions = MutableStateFlow(false)
    val isLoadingSessions: StateFlow<Boolean> = _isLoadingSessions.asStateFlow()

    /** 会话列表是否还有更多（分页） */
    private val _sessionsHasMore = MutableStateFlow(false)
    val sessionsHasMore: StateFlow<Boolean> = _sessionsHasMore.asStateFlow()

    private var historyNextCursor: String? = null
    private var historyHasMore: Boolean = false

    // ---------- 打字机逐字缓冲 ----------
    private val charBuffer = ArrayDeque<Char>()
    private val bufferMutex = Mutex()
    @Volatile
    private var pendingFinalText: String? = null   // 流结束后的完整文本；空串表示出错终止
    @Volatile
    private var typewriterRunning = false

    init {
        loadSessions()
        loadUserInfo()
        initSession()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            userRepository.getUserInfo(object : UserRepository.AuthCallback<UserInfoResponse> {
                override fun onSuccess(response: UserInfoResponse?) {
                    if (response != null && response.code == 200 && response.data != null) {
                        val avatar = response.data.avatar
                        _userAvatar.value = avatar
                        Log.d(TAG, "User avatar loaded: $avatar")
                    }
                }

                override fun onError(errorMessage: String?) {
                    Log.e(TAG, "Failed to load user info: $errorMessage")
                }
            })
        }
    }

    // ---------- 会话列表（远端） ----------
    fun loadSessions() {
        _isLoadingSessions.value = true
        repository.loadSessions { sessions, _, hasMore, error ->
            _isLoadingSessions.value = false
            _sessionsHasMore.value = hasMore
            if (error == null) {
                _chatSessions.value = sessions
            } else {
                Log.e(TAG, "加载会话列表失败: $error")
            }
        }
    }

    fun loadMoreSessions() {
        val cursor = repository.sessionsNextCursor ?: return
        if (!repository.sessionsHasMore || _isLoadingSessions.value) return
        _isLoadingSessions.value = true
        repository.loadSessions(cursor) { sessions, _, hasMore, error ->
            _isLoadingSessions.value = false
            _sessionsHasMore.value = hasMore
            if (error == null) {
                _chatSessions.value = _chatSessions.value + sessions
            }
        }
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    // ---------- 欢迎语（本地打字机） ----------
    fun initSession() {
        _isWelcomeStreaming.value = true
        _welcomeMessage.value = ""
        _showWelcome.value = true

        viewModelScope.launch {
            val welcomeText = "您好！我是智能客服助手，很高兴为您服务。\n\n我可以帮助您：\n• 查询贷款申请进度\n• 计算还款金额或还款计划\n• 了解我们的贷款产品\n• 咨询最新贷款政策\n\n请问有什么可以帮助您的吗？"

            welcomeText.forEach { char ->
                delay(30)
                _welcomeMessage.value += char
            }

            _isWelcomeStreaming.value = false
            Log.d(TAG, "Welcome message completed")
        }
    }

    // ---------- 发送消息（SSE + 打字机） ----------
    fun sendMessage() {
        val message = _inputText.value.trim()
        if (message.isEmpty()) {
            return
        }
        if (_isStreaming.value) {
            return
        }

        _inputText.value = ""
        _errorMessage.value = null
        _showWelcome.value = false

        val userMessage = ChatMessage(content = message, isFromUser = true)
        _messages.value = _messages.value + userMessage

        _isLoading.value = true
        _isStreaming.value = true
        _currentStreamingMessage.value = ""
        pendingFinalText = null
        startTypewriter()

        repository.sendMessage(
            message = message,
            sessionId = _currentSessionId.value,
            onSessionInit = { sessionId ->
                _currentSessionId.value = sessionId
                Log.d(TAG, "Session initialized: $sessionId")
            },
            onMessageChunk = { chunk ->
                // 网络回调线程：入队等待打字机协程逐字放出
                viewModelScope.launch {
                    bufferMutex.withLock {
                        chunk.forEach { charBuffer.addLast(it) }
                    }
                }
            },
            onError = { error ->
                Log.e(TAG, "Error: $error")
                _errorMessage.value = error
                // 终止打字机：已显示部分保留，不追加为正式消息
                pendingFinalText = ""
            },
            onComplete = { fullText ->
                pendingFinalText = fullText
            }
        )
    }

    /**
     * 打字机消费协程：从缓冲队列逐字放出；队列耗尽且流已结束时定格完整文本
     */
    private fun startTypewriter() {
        if (typewriterRunning) return
        typewriterRunning = true
        viewModelScope.launch {
            while (true) {
                val next = bufferMutex.withLock { charBuffer.removeFirstOrNull() }
                when {
                    next != null -> {
                        _currentStreamingMessage.value += next
                        delay(TYPEWRITER_DELAY_MS)
                    }
                    else -> {
                        val finalText = pendingFinalText
                        if (finalText != null) {
                            // 流已结束且缓冲耗尽
                            if (finalText.isNotEmpty()) {
                                _currentStreamingMessage.value = finalText
                                appendAiMessage(finalText)
                            } else {
                                _currentStreamingMessage.value = ""
                            }
                            pendingFinalText = null
                            _isLoading.value = false
                            _isStreaming.value = false
                            typewriterRunning = false
                            // 刷新会话列表（后端已落库，预览/标题更新）
                            loadSessions()
                            break
                        }
                        delay(20)
                    }
                }
            }
        }
    }

    private fun appendAiMessage(content: String) {
        val aiMessage = ChatMessage(content = content, isFromUser = false)
        _messages.value = _messages.value + aiMessage
    }

    fun cancelCurrentRequest() {
        repository.cancelCurrentRequest()
        _isLoading.value = false
        _isStreaming.value = false
        _currentStreamingMessage.value = ""
        pendingFinalText = null
        viewModelScope.launch {
            bufferMutex.withLock { charBuffer.clear() }
        }
    }

    // ---------- 新建会话（POST /api/sessions） ----------
    fun createNewSession() {
        repository.createSession { session, error ->
            if (session != null) {
                _currentSessionId.value = session.id
                _messages.value = emptyList()
                // 重新播放打字机欢迎语
                initSession()
                // 立即把新会话插入列表头部（后端空会话复用策略兜底）
                _chatSessions.value = listOf(session) + _chatSessions.value.filter { it.id != session.id }
            } else {
                _errorMessage.value = error ?: "新建会话失败"
            }
        }
    }

    // ---------- 切换会话（拉取历史） ----------
    fun switchSession(sessionId: String) {
        if (_isStreaming.value) {
            cancelCurrentRequest()
        }
        _currentSessionId.value = sessionId
        repository.setCurrentSessionId(sessionId)
        _isLoading.value = true

        repository.fetchMessages(sessionId) { msgs, nextCursor, hasMore, error ->
            _isLoading.value = false
            if (error == null) {
                _messages.value = msgs
                historyNextCursor = nextCursor
                historyHasMore = hasMore
                _showWelcome.value = msgs.isEmpty()
                _currentStreamingMessage.value = ""
            } else {
                _errorMessage.value = error
            }
        }
    }

    // ---------- 向上加载更早历史 ----------
    fun loadOlderMessages() {
        val sessionId = _currentSessionId.value ?: return
        if (!historyHasMore || _isLoadingOlder.value || historyNextCursor.isNullOrBlank()) return

        _isLoadingOlder.value = true
        repository.fetchMessages(sessionId, historyNextCursor) { msgs, nextCursor, hasMore, error ->
            _isLoadingOlder.value = false
            if (error == null) {
                _messages.value = msgs + _messages.value   // 前置插入（后端已按时间升序）
                historyNextCursor = nextCursor
                historyHasMore = hasMore
            } else {
                _errorMessage.value = error
            }
        }
    }

    // ---------- 重命名 ----------
    fun renameSession(sessionId: String, title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        repository.renameSession(sessionId, trimmed) { _, error ->
            if (error != null) {
                _errorMessage.value = error
            }
            loadSessions()
        }
    }

    // ---------- 置顶/取消置顶 ----------
    fun togglePin(sessionId: String, pinned: Boolean) {
        repository.setPinned(sessionId, pinned) { _, error ->
            if (error != null) {
                _errorMessage.value = error
            }
            loadSessions()
        }
    }

    fun pinSessions(sessionIds: List<String>, pinned: Boolean) {
        if (sessionIds.isEmpty()) return
        repository.setPinnedBatch(sessionIds, pinned) { _, error ->
            if (error != null) {
                _errorMessage.value = error
            }
            loadSessions()
        }
    }

    // ---------- 删除（单/批） ----------
    fun deleteSession(sessionId: String) {
        repository.deleteSession(sessionId) { _, error ->
            if (error != null) {
                _errorMessage.value = error
            }
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
                _messages.value = emptyList()
                _showWelcome.value = true
            }
            loadSessions()
        }
    }

    fun deleteSessions(sessionIds: List<String>) {
        if (sessionIds.isEmpty()) return
        repository.deleteSessionBatch(sessionIds) { _, error ->
            if (error != null) {
                _errorMessage.value = error
            }
            if (_currentSessionId.value != null && _currentSessionId.value in sessionIds) {
                _currentSessionId.value = null
                _messages.value = emptyList()
                _showWelcome.value = true
            }
            loadSessions()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
