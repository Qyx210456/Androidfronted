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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "ChatViewModel"
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
    
    private fun loadSessions() {
        viewModelScope.launch {
            val sessions = repository.getAllSessions()
            _chatSessions.value = sessions
            Log.d(TAG, "Loaded ${sessions.size} sessions")
        }
    }
    
    fun updateInputText(text: String) {
        _inputText.value = text
    }
    
    fun initSession() {
        _isWelcomeStreaming.value = true
        _welcomeMessage.value = ""
        _showWelcome.value = true
        
        if (_currentSessionId.value == null) {
            _currentSessionId.value = UUID.randomUUID().toString()
        }
        
        viewModelScope.launch {
            val welcomeText = "您好！我是智能客服助手，很高兴为您服务。\n\n我可以帮助您：\n• 查询贷款申请进度\n• 计算还款金额或还款计划\n• 了解我们的贷款产品\n• 咨询最新贷款政策\n\n请问有什么可以帮助您的吗？"
            
            withContext(Dispatchers.IO) {
                welcomeText.forEach { char ->
                    delay(30)
                    withContext(Dispatchers.Main) {
                        _welcomeMessage.value += char
                    }
                }
            }
            
            _isWelcomeStreaming.value = false
            Log.d(TAG, "Welcome message completed")
        }
    }
    
    fun sendMessage() {
        val message = _inputText.value.trim()
        if (message.isEmpty()) {
            return
        }
        
        _inputText.value = ""
        _errorMessage.value = null
        _showWelcome.value = false
        
        val userMessage = ChatMessage(
            content = message,
            isFromUser = true
        )
        
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(userMessage)
        _messages.value = currentMessages
        
        _isLoading.value = true
        _isStreaming.value = true
        _currentStreamingMessage.value = ""
        
        viewModelScope.launch {
            repository.sendMessage(
                message = message,
                currentMessages = currentMessages,
                onSessionInit = { sessionId ->
                    _currentSessionId.value = sessionId
                    Log.d(TAG, "Session initialized: $sessionId")
                },
                onMessageChunk = { chunk ->
                    _currentStreamingMessage.value += chunk
                },
                onToolCall = { toolName, arguments ->
                    Log.d(TAG, "Tool called: $toolName with arguments: $arguments")
                },
                onError = { error ->
                    _errorMessage.value = error
                    _isLoading.value = false
                    _isStreaming.value = false
                    Log.e(TAG, "Error: $error")
                },
                onComplete = { finalMessages ->
                    _messages.value = finalMessages
                    _chatSessions.value = repository.getAllSessions()
                    _isLoading.value = false
                    _isStreaming.value = false
                    _currentStreamingMessage.value = ""
                    Log.d(TAG, "Message sent successfully")
                }
            )
        }
    }
    
    fun cancelCurrentRequest() {
        repository.cancelCurrentRequest()
        _isLoading.value = false
        _isStreaming.value = false
        _currentStreamingMessage.value = ""
        Log.d(TAG, "Request cancelled")
    }
    
    fun createNewSession() {
        val session = repository.createNewSession()
        _currentSessionId.value = session.id
        _messages.value = emptyList()
        _chatSessions.value = repository.getAllSessions()
        _showWelcome.value = true
        initSession()
        Log.d(TAG, "Created new session: ${session.id}")
    }
    
    fun switchSession(sessionId: String) {
        val session = repository.switchSession(sessionId)
        if (session != null) {
            _currentSessionId.value = sessionId
            _messages.value = session.messages
            Log.d(TAG, "Switched to session: $sessionId")
        }
    }
    
    fun deleteSession(sessionId: String) {
        repository.deleteSession(sessionId)
        _chatSessions.value = repository.getAllSessions()
        
        if (_currentSessionId.value == sessionId) {
            val currentSession = repository.getCurrentSession()
            _currentSessionId.value = currentSession?.id
            _messages.value = currentSession?.messages ?: emptyList()
        }
        
        Log.d(TAG, "Deleted session: $sessionId")
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
