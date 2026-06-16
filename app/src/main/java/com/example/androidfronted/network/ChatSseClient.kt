package com.example.androidfronted.network

import android.content.Context
import android.util.Log
import com.example.androidfronted.data.model.smartcustomerservice.ChatRequest
import com.example.androidfronted.data.model.smartcustomerservice.ToolCallInfo
import com.example.androidfronted.util.TokenManager
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.BufferedSource
import java.io.IOException
import java.util.concurrent.TimeUnit

class ChatSseClient(private val context: Context) {
    
    companion object {
        private const val TAG = "ChatSseClient"
        private const val BASE_URL = "http://10.0.2.2:8080"  
        private const val CHAT_ENDPOINT = "/api/chat"  // Java后端SSE流式接口
    }
    
    private val okHttpClient: OkHttpClient = NetworkClient.getOkHttpClient(context)
        .newBuilder()
        .readTimeout(0, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private var currentCall: Call? = null
    
    fun initSession(
        onSessionInit: (String) -> Unit,
        onMessage: (String) -> Unit,
        onError: (String) -> Unit,
        onComplete: () -> Unit
    ) {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()
        
        // 添加详细的token日志
        Log.d(TAG, "=== TOKEN验证开始 ===")
        Log.d(TAG, "Token存在: ${token != null}")
        Log.d(TAG, "Token长度: ${token?.length ?: 0}")
        if (token != null && token.isNotEmpty()) {
            Log.d(TAG, "完整Token: $token")
            Log.d(TAG, "Token前20字符: ${token.substring(0, Math.min(20, token.length))}")
            
            // 验证token格式
            val parts = token.split(".")
            Log.d(TAG, "Token分段数: ${parts.size}")
            if (parts.size >= 2) {
                Log.d(TAG, "Token格式正确（JWT格式）")
            } else {
                Log.e(TAG, "Token格式错误（不是JWT格式）")
                onError("Token格式错误")
                return
            }
            
            // 验证token是否有效（未过期）
            val isValid = tokenManager.isTokenValid()
            Log.d(TAG, "Token有效性: $isValid")
            if (!isValid) {
                Log.e(TAG, "Token已过期或无效")
                onError("Token已过期，请重新登录")
                return
            }
        } else {
            Log.e(TAG, "Token为空或不存在")
            onError("未登录或Token无效")
            return
        }
        Log.d(TAG, "=== TOKEN验证通过 ===")
        
        val request = ChatRequest(message = "你好", sessionId = null, agentMode = "react")
        val jsonBody = gson.toJson(request)
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        
        Log.d(TAG, "Init session request body: $jsonBody")
        
        val httpRequest = Request.Builder()
            .url("$BASE_URL$CHAT_ENDPOINT")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()
        
        Log.d(TAG, "Init session request URL: ${httpRequest.url}")
        Log.d(TAG, "Init session request headers: ${httpRequest.headers}")
        Log.d(TAG, "Authorization header: ${httpRequest.header("Authorization")}")
        
        currentCall = okHttpClient.newCall(httpRequest)
        
        currentCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "SSE connection failed", e)
                onError(e.message ?: "网络连接失败")
            }
            
            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response headers: ${response.headers}")
                
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No error body"
                    Log.e(TAG, "Request failed with code ${response.code}: $errorBody")
                    onError("请求失败: ${response.code} - $errorBody")
                    return
                }
                
                Log.d(TAG, "Response successful, parsing SSE stream...")
                
                response.body?.source()?.use { source ->
                    try {
                        parseSseStream(
                            source = source,
                            onSessionInit = onSessionInit,
                            onMessage = onMessage,
                            onToolCall = { },
                            onToolResult = { },
                            onError = onError
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing SSE stream", e)
                        onError(e.message ?: "解析响应失败")
                    }
                }
                
                onComplete()
            }
        })
    }
    
    fun streamChat(
        request: ChatRequest,
        onSessionInit: (String) -> Unit,
        onMessage: (String) -> Unit,
        onToolCall: (ToolCallInfo) -> Unit,
        onToolResult: (String) -> Unit,
        onError: (String) -> Unit,
        onComplete: () -> Unit
    ) {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getToken()
        
        // 添加详细的token日志
        Log.d(TAG, "=== TOKEN验证开始 ===")
        Log.d(TAG, "Token存在: ${token != null}")
        Log.d(TAG, "Token长度: ${token?.length ?: 0}")
        if (token != null && token.isNotEmpty()) {
            Log.d(TAG, "完整Token: $token")
            Log.d(TAG, "Token前20字符: ${token.substring(0, Math.min(20, token.length))}")
            
            // 验证token格式
            val parts = token.split(".")
            Log.d(TAG, "Token分段数: ${parts.size}")
            if (parts.size >= 2) {
                Log.d(TAG, "Token格式正确（JWT格式）")
            } else {
                Log.e(TAG, "Token格式错误（不是JWT格式）")
                onError("Token格式错误")
                return
            }
            
            // 验证token是否有效（未过期）
            val isValid = tokenManager.isTokenValid()
            Log.d(TAG, "Token有效性: $isValid")
            if (!isValid) {
                Log.e(TAG, "Token已过期或无效")
                onError("Token已过期，请重新登录")
                return
            }
        } else {
            Log.e(TAG, "Token为空或不存在")
            onError("未登录或Token无效")
            return
        }
        Log.d(TAG, "=== TOKEN验证通过 ===")
        
        val jsonBody = gson.toJson(request)
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        
        Log.d(TAG, "Stream chat request body: $jsonBody")
        
        val httpRequest = Request.Builder()
            .url("$BASE_URL$CHAT_ENDPOINT")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()
        
        Log.d(TAG, "Stream chat request URL: ${httpRequest.url}")
        Log.d(TAG, "Stream chat request headers: ${httpRequest.headers}")
        Log.d(TAG, "Authorization header: ${httpRequest.header("Authorization")}")
        
        currentCall = okHttpClient.newCall(httpRequest)
        
        currentCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "SSE connection failed", e)
                onError(e.message ?: "网络连接失败")
            }
            
            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response headers: ${response.headers}")
                
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No error body"
                    Log.e(TAG, "Request failed with code ${response.code}: $errorBody")
                    onError("请求失败: ${response.code} - $errorBody")
                    return
                }
                
                Log.d(TAG, "Response successful, parsing SSE stream...")
                
                response.body?.source()?.use { source ->
                    try {
                        parseSseStream(
                            source = source,
                            onSessionInit = onSessionInit,
                            onMessage = onMessage,
                            onToolCall = onToolCall,
                            onToolResult = onToolResult,
                            onError = onError
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing SSE stream", e)
                        onError(e.message ?: "解析响应失败")
                    }
                }
                
                onComplete()
            }
        })
    }
    
    private fun parseSseStream(
        source: BufferedSource,
        onSessionInit: (String) -> Unit,
        onMessage: (String) -> Unit,
        onToolCall: (ToolCallInfo) -> Unit,
        onToolResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        var currentEvent: String? = null
        
        Log.d(TAG, "=== 开始解析SSE流 ===")
        
        while (!source.exhausted()) {
            val line = source.readUtf8Line() ?: continue
            
            Log.d(TAG, "SSE原始行: '$line'")
            Log.d(TAG, "行长度: ${line.length}")
            
            when {
                line.startsWith("event:") -> {
                    currentEvent = line.substringAfter("event:").trim()
                    Log.d(TAG, "解析到event: '$currentEvent'")
                }
                line.startsWith("data:") -> {
                    val rawValue = line.substringAfter("data:")
                    val data = if (rawValue.startsWith(" ")) rawValue.substring(1) else rawValue
                    
                    Log.d(TAG, "解析到data: '$data'")
                    Log.d(TAG, "当前event类型: '$currentEvent'")
                    
                    if (data.isEmpty()) {
                        if (currentEvent == "message") {
                            onMessage("\n")
                        }
                        continue
                    }
                    
                    handleSseEvent(
                        event = currentEvent,
                        data = data,
                        onSessionInit = onSessionInit,
                        onMessage = onMessage,
                        onToolCall = onToolCall,
                        onToolResult = onToolResult,
                        onError = onError
                    )
                }
                line.isEmpty() -> {
                    Log.d(TAG, "遇到空行，重置event类型")
                    currentEvent = null
                }
                else -> {
                    Log.w(TAG, "未识别的SSE行: '$line'")
                }
            }
        }
        
        Log.d(TAG, "=== SSE流解析完成 ===")
    }
    
    private fun handleSseEvent(
        event: String?,
        data: String,
        onSessionInit: (String) -> Unit,
        onMessage: (String) -> Unit,
        onToolCall: (ToolCallInfo) -> Unit,
        onToolResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d(TAG, "handleSseEvent: event='$event', data='$data'")
        
        // 如果event为null，尝试从data内容推断事件类型
        if (event == null) {
            Log.d(TAG, "Event类型为null，尝试从data内容推断事件类型")
            
            // 检查是否是session_init事件
            if (data.contains("session_id")) {
                Log.d(TAG, "推断为session_init事件")
                try {
                    val jsonObject = JsonParser.parseString(data).asJsonObject
                    val sessionId = jsonObject.get("session_id")?.asString
                    if (sessionId != null) {
                        onSessionInit(sessionId)
                        Log.d(TAG, "Session initialized: $sessionId")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse session_init", e)
                }
                return
            }
            
            // 检查是否是错误事件
            if (data.contains("发生错误") || data.contains("Error") || data.contains("AuthenticationRequired")) {
                Log.e(TAG, "推断为error事件（仅记录日志，不反馈到屏幕）: $data")
                // 不调用onError，只记录日志
                return
            }
            
            // 检查是否是JSON格式的其他事件
            try {
                val jsonObject = JsonParser.parseString(data).asJsonObject
                
                // 检查是否有type字段
                val type = jsonObject.get("type")?.asString
                if (type != null) {
                    Log.d(TAG, "从JSON中推断事件类型: $type")
                    when (type) {
                        "message" -> {
                            val content = jsonObject.get("content")?.asString ?: ""
                            Log.d(TAG, "收到message事件: $content")
                            onMessage(content)
                        }
                        "tool_call" -> {
                            val toolName = jsonObject.get("tool_name")?.asString ?: ""
                            @Suppress("UNCHECKED_CAST")
                            val arguments = jsonObject.get("arguments")?.asJsonObject?.let { 
                                gson.fromJson(it, Map::class.java) as Map<String, Any>
                            } ?: emptyMap()
                            onToolCall(ToolCallInfo(toolName, arguments))
                        }
                        "tool_result" -> {
                            val result = jsonObject.get("result")?.asString ?: data
                            onToolResult(result)
                        }
                        "error" -> {
                            val message = jsonObject.get("message")?.asString ?: data
                            onError(message)
                        }
                        else -> {
                            Log.w(TAG, "未知的JSON事件类型: $type")
                        }
                    }
                    return
                }
            } catch (e: Exception) {
                Log.d(TAG, "Data不是JSON格式，当作普通消息处理")
            }
            
            // 如果都不是，当作普通消息处理
            Log.d(TAG, "推断为message事件")
            onMessage(data)
            return
        }
        
        // 正常处理有event类型的SSE事件
        when (event) {
            "session_init" -> {
                try {
                    val jsonObject = JsonParser.parseString(data).asJsonObject
                    val sessionId = jsonObject.get("session_id")?.asString
                    if (sessionId != null) {
                        onSessionInit(sessionId)
                        Log.d(TAG, "Session initialized: $sessionId")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse session_init", e)
                }
            }
            "message" -> {
                Log.d(TAG, "原始message数据: $data")
                Log.d(TAG, "数据长度: ${data.length}, 包含\\n: ${data.contains("\\n")}")
                onMessage(data)
            }
            "tool_call" -> {
                try {
                    val jsonData = data.replace("'", "\"")
                    val toolCallMap = gson.fromJson(jsonData, Map::class.java)
                    val toolName = toolCallMap["tool_name"] as? String ?: ""
                    @Suppress("UNCHECKED_CAST")
                    val arguments = toolCallMap["arguments"] as? Map<String, Any> ?: emptyMap()
                    onToolCall(ToolCallInfo(toolName, arguments))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse tool_call: $data", e)
                }
            }
            "tool_result" -> {
                try {
                    val jsonData = data.replace("'", "\"")
                    val resultMap = gson.fromJson(jsonData, Map::class.java)
                    val result = resultMap["result"] as? String ?: data
                    onToolResult(result)
                } catch (e: Exception) {
                    onToolResult(data)
                }
            }
            "error" -> {
                Log.e(TAG, "收到error事件: $data")
                onError(data)
            }
            else -> {
                Log.w(TAG, "Unknown event type: $event")
            }
        }
    }
    
    fun cancel() {
        currentCall?.cancel()
        currentCall = null
        Log.d(TAG, "SSE request cancelled")
    }
}
