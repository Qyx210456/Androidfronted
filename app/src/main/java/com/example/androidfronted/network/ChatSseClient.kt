package com.example.androidfronted.network

import android.content.Context
import android.util.Log
import com.example.androidfronted.data.model.smartcustomerservice.ChatRequest
import com.example.androidfronted.data.model.smartcustomerservice.ToolCallInfo
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
        private const val BASE_URL = "http://10.0.2.2:8000"
        private const val CHAT_ENDPOINT = "/api/chat/stream"
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
        val request = ChatRequest(message = "你好", session_id = null)
        val jsonBody = gson.toJson(request)
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        
        val httpRequest = Request.Builder()
            .url("$BASE_URL$CHAT_ENDPOINT")
            .post(requestBody)
            .build()
        
        currentCall = okHttpClient.newCall(httpRequest)
        
        currentCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "SSE connection failed", e)
                onError(e.message ?: "网络连接失败")
            }
            
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onError("请求失败: ${response.code}")
                    return
                }
                
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
        val jsonBody = gson.toJson(request)
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        
        val httpRequest = Request.Builder()
            .url("$BASE_URL$CHAT_ENDPOINT")
            .post(requestBody)
            .build()
        
        currentCall = okHttpClient.newCall(httpRequest)
        
        currentCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "SSE connection failed", e)
                onError(e.message ?: "网络连接失败")
            }
            
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onError("请求失败: ${response.code}")
                    return
                }
                
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
        
        while (!source.exhausted()) {
            val line = source.readUtf8Line() ?: continue
            
            when {
                line.startsWith("event:") -> {
                    currentEvent = line.substringAfter("event:").trim()
                }
                line.startsWith("data:") -> {
                    val rawValue = line.substringAfter("data:")
                    val data = if (rawValue.startsWith(" ")) rawValue.substring(1) else rawValue
                    
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
            }
        }
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
        when (event) {
            "session_init" -> {
                try {
                    val jsonObject = JsonParser.parseString(data).asJsonObject
                    val sessionId = jsonObject.get("session_id")?.asString
                    if (sessionId != null) {
                        onSessionInit(sessionId)
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
