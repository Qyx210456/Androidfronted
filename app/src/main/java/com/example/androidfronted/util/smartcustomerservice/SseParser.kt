package com.example.androidfronted.util.smartcustomerservice

import android.util.Log
import com.google.gson.JsonParser

object SseParser {
    
    private const val TAG = "SseParser"
    
    fun parseSseLine(line: String): SseEvent? {
        if (line.isBlank()) return null
        
        return when {
            line.startsWith("event:") -> {
                SseEvent.EventType(line.substringAfter("event:").trim())
            }
            line.startsWith("data:") -> {
                SseEvent.EventData(line.substringAfter("data:").trim())
            }
            line.startsWith("id:") -> {
                SseEvent.EventId(line.substringAfter("id:").trim())
            }
            line.startsWith("retry:") -> {
                SseEvent.EventRetry(line.substringAfter("retry:").trim().toLongOrNull() ?: 0)
            }
            else -> null
        }
    }
    
    fun parseSessionInit(data: String): String? {
        return try {
            val jsonObject = JsonParser.parseString(data).asJsonObject
            jsonObject.get("session_id")?.asString
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse session_init", e)
            null
        }
    }
    
    fun parseToolCall(data: String): ToolCallData? {
        return try {
            val jsonObject = JsonParser.parseString(data).asJsonObject
            val toolName = jsonObject.get("tool_name")?.asString ?: return null
            val arguments = jsonObject.get("arguments")?.asJsonObject ?: return null
            
            val argsMap = mutableMapOf<String, Any>()
            arguments.entrySet().forEach { entry ->
                argsMap[entry.key] = when {
                    entry.value.isJsonPrimitive -> {
                        val primitive = entry.value.asJsonPrimitive
                        when {
                            primitive.isBoolean -> primitive.asBoolean
                            primitive.isNumber -> primitive.asNumber
                            else -> primitive.asString
                        }
                    }
                    else -> entry.value.toString()
                }
            }
            
            ToolCallData(toolName, argsMap)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse tool_call", e)
            null
        }
    }
}

sealed class SseEvent {
    data class EventType(val type: String) : SseEvent()
    data class EventData(val data: String) : SseEvent()
    data class EventId(val id: String) : SseEvent()
    data class EventRetry(val retry: Long) : SseEvent()
}

data class ToolCallData(
    val toolName: String,
    val arguments: Map<String, Any>
)
