/**
 * 会话/消息接口服务（对齐 session.md 的 7 个接口）
 *
 * - 统一外层包装 {code, message, data}，手动解析外层后再映射 data
 * - 回调统一切回主线程
 * - 401/404 等错误码随 onError 透出，供上层区分处理
 */
package com.example.androidfronted.data.source

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.androidfronted.data.model.smartcustomerservice.MessageItem
import com.example.androidfronted.data.model.smartcustomerservice.MessagePage
import com.example.androidfronted.data.model.smartcustomerservice.SessionItem
import com.example.androidfronted.data.model.smartcustomerservice.SessionPage
import com.example.androidfronted.network.NetworkClient
import com.example.androidfronted.util.TokenManager
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class SessionApiService(private val context: Context) {

    companion object {
        private const val TAG = "SessionApiService"
        /** 会话接口直连 Python 服务（8000）：session.md 的 7 个接口仅在 Python 侧实现，Java 网关未路由 */
        private const val BASE_URL = "http://10.0.2.2:8000"
        private const val SESSIONS_URL = "$BASE_URL/api/sessions"
    }

    interface ApiCallback<T> {
        fun onSuccess(result: T)
        fun onError(message: String, code: Int = -1)
    }

    private val client = NetworkClient.getOkHttpClient(context)
        .newBuilder()
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())

    // ---------- 1. 新建会话 POST /api/sessions ----------
    fun createSession(callback: ApiCallback<SessionItem>) {
        request("POST", SESSIONS_URL, null, object : ApiCallback<String> {
            override fun onSuccess(result: String) {
                val data = parseData(result, SessionItem::class.java)
                if (data != null) callback.onSuccess(data)
                else callback.onError("创建会话响应解析失败", -1)
            }

            override fun onError(message: String, code: Int) = callback.onError(message, code)
        })
    }

    // ---------- 2. 查询会话列表 GET /api/sessions ----------
    fun getSessions(
        cursor: String? = null,
        limit: Int = 20,
        includeArchived: Boolean = false,
        hasMessages: Boolean = false,
        callback: ApiCallback<SessionPage>
    ) {
        val url = buildString {
            append(SESSIONS_URL)
            append("?limit=").append(limit)
            append("&include_archived=").append(includeArchived)
            if (hasMessages) append("&has_messages=true")
            if (!cursor.isNullOrBlank()) append("&cursor=").append(java.net.URLEncoder.encode(cursor, "UTF-8"))
        }
        request("GET", url, null, object : ApiCallback<String> {
            override fun onSuccess(result: String) {
                try {
                    val data = JsonParser.parseString(result).asJsonObject.getAsJsonObject("data")
                    val items = gson.fromJson(data.getAsJsonArray("items"), Array<SessionItem>::class.java)
                    val nextCursor = data.get("next_cursor")?.takeIf { !it.isJsonNull }?.asString
                    val hasMore = data.get("has_more")?.asBoolean ?: false
                    callback.onSuccess(SessionPage(items.toList(), nextCursor, hasMore))
                } catch (e: Exception) {
                    Log.e(TAG, "解析会话列表失败", e)
                    callback.onError("会话列表解析失败", -1)
                }
            }

            override fun onError(message: String, code: Int) = callback.onError(message, code)
        })
    }

    // ---------- 3. 查询单个会话 GET /api/sessions/{id} ----------
    fun getSession(sessionId: String, callback: ApiCallback<SessionItem>) {
        request("GET", "$SESSIONS_URL/$sessionId", null, object : ApiCallback<String> {
            override fun onSuccess(result: String) {
                val data = parseData(result, SessionItem::class.java)
                if (data != null) callback.onSuccess(data)
                else callback.onError("获取会话解析失败", -1)
            }

            override fun onError(message: String, code: Int) = callback.onError(message, code)
        })
    }

    // ---------- 4. 更新会话 PATCH /api/sessions/{id}（任意子集） ----------
    fun updateSession(
        sessionId: String,
        title: String? = null,
        pinned: Boolean? = null,
        archived: Boolean? = null,
        callback: ApiCallback<SessionItem>
    ) {
        val body = StringBuilder("{")
        var first = true
        title?.let {
            if (!first) body.append(",")
            body.append("\"title\":").append(gson.toJson(it))
            first = false
        }
        pinned?.let {
            if (!first) body.append(",")
            body.append("\"pinned\":").append(it)
            first = false
        }
        archived?.let {
            if (!first) body.append(",")
            body.append("\"archived\":").append(it)
        }
        body.append("}")
        request("PATCH", "$SESSIONS_URL/$sessionId", body.toString(), object : ApiCallback<String> {
            override fun onSuccess(result: String) {
                val data = parseData(result, SessionItem::class.java)
                if (data != null) callback.onSuccess(data)
                else callback.onError("更新会话解析失败", -1)
            }

            override fun onError(message: String, code: Int) = callback.onError(message, code)
        })
    }

    // ---------- 5. 置顶/取消置顶 POST /api/sessions/{id}/pin ----------
    fun pinSession(sessionId: String, pinned: Boolean, callback: ApiCallback<SessionItem>) {
        val body = "{\"pinned\":$pinned}"
        request("POST", "$SESSIONS_URL/$sessionId/pin", body, object : ApiCallback<String> {
            override fun onSuccess(result: String) {
                val data = parseData(result, SessionItem::class.java)
                if (data != null) callback.onSuccess(data)
                else callback.onError("置顶响应解析失败", -1)
            }

            override fun onError(message: String, code: Int) = callback.onError(message, code)
        })
    }

    // ---------- 6. 删除会话 DELETE /api/sessions/{id} ----------
    fun deleteSession(sessionId: String, callback: ApiCallback<String>) {
        request("DELETE", "$SESSIONS_URL/$sessionId", null, object : ApiCallback<String> {
            override fun onSuccess(result: String) {
                val root = JsonParser.parseString(result).asJsonObject
                val deletedId = root.getAsJsonObject("data")?.get("session_id")?.asString ?: sessionId
                callback.onSuccess(deletedId)
            }

            override fun onError(message: String, code: Int) = callback.onError(message, code)
        })
    }

    // ---------- 7. 分页查询会话消息 GET /api/sessions/{id}/messages ----------
    fun getMessages(
        sessionId: String,
        cursor: String? = null,
        limit: Int = 30,
        callback: ApiCallback<MessagePage>
    ) {
        val url = buildString {
            append("$SESSIONS_URL/$sessionId/messages?limit=").append(limit)
            if (!cursor.isNullOrBlank()) append("&cursor=").append(java.net.URLEncoder.encode(cursor, "UTF-8"))
        }
        request("GET", url, null, object : ApiCallback<String> {
            override fun onSuccess(result: String) {
                try {
                    val data = JsonParser.parseString(result).asJsonObject.getAsJsonObject("data")
                    val items = gson.fromJson(data.getAsJsonArray("items"), Array<MessageItem>::class.java)
                    val nextCursor = data.get("next_cursor")?.takeIf { !it.isJsonNull }?.asString
                    val hasMore = data.get("has_more")?.asBoolean ?: false
                    callback.onSuccess(MessagePage(items.toList(), nextCursor, hasMore))
                } catch (e: Exception) {
                    Log.e(TAG, "解析消息列表失败", e)
                    callback.onError("消息列表解析失败", -1)
                }
            }

            override fun onError(message: String, code: Int) = callback.onError(message, code)
        })
    }

    // ---------- 内部通用 ----------
    /** 解析外层 {code,message,data}，data 为对象时使用 */
    private fun <T> parseData(json: String, clazz: Class<T>): T? {
        return try {
            val root = JsonParser.parseString(json).asJsonObject
            val code = root.get("code")?.asInt ?: -1
            if (code != 200) return null
            gson.fromJson(root.getAsJsonObject("data"), clazz)
        } catch (e: Exception) {
            Log.e(TAG, "parseData失败", e)
            null
        }
    }

    private fun request(method: String, url: String, body: String?, callback: ApiCallback<String>) {
        val token = TokenManager(context).getToken()
        if (token.isNullOrEmpty()) {
            mainHandler.post { callback.onError("未登录或登录已过期", 401) }
            return
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = when (method) {
            "POST", "PATCH" -> (body ?: "{}").toRequestBody(mediaType)
            else -> null
        }

        val builder = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
        when (method) {
            "POST" -> builder.post(requestBody!!)
            "PATCH" -> builder.patch(requestBody!!)
            "DELETE" -> builder.delete()
            else -> builder.get()
        }

        client.newCall(builder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "$method $url 失败", e)
                mainHandler.post { callback.onError("网络错误", -1) }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: "{}"
                val httpCode = response.code
                Log.d(TAG, "$method $url -> $httpCode: $responseBody")
                mainHandler.post {
                    if (!response.isSuccessful) {
                        val msg = extractMessage(responseBody) ?: "请求失败: $httpCode"
                        val bizCode = when (httpCode) {
                            401 -> 401
                            404 -> 404
                            else -> httpCode
                        }
                        callback.onError(msg, bizCode)
                        return@post
                    }
                    val root = try {
                        JsonParser.parseString(responseBody).asJsonObject
                    } catch (e: Exception) {
                        callback.onError("响应解析失败", -1)
                        return@post
                    }
                    val code = root.get("code")?.asInt ?: httpCode
                    if (code != 200) {
                        callback.onError(root.get("message")?.asString ?: "请求失败", code)
                        return@post
                    }
                    callback.onSuccess(responseBody)
                }
            }
        })
    }

    private fun extractMessage(json: String): String? = try {
        JsonParser.parseString(json).asJsonObject.get("message")?.asString
    } catch (e: Exception) {
        null
    }
}
