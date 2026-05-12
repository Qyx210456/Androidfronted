/**
 * 侧边栏组件
 * 
 * 该文件包含智能客服聊天界面的侧边栏组件，包括：
 * - SideDrawer: 侧边栏主组件
 * - DrawerHeader: 侧边栏头部（标题和搜索框）
 * - SessionItem: 会话列表项
 * 
 * 功能说明：
 * 1. 显示聊天会话列表
 * 2. 支持搜索会话记录
 * 3. 支持切换会话
 * 4. 支持删除会话
 * 
 * UI尺寸参考：
 * - 侧边栏宽度: 280.dp
 * - 头部内边距: 16.dp
 * - 搜索框圆角: 12.dp
 * - 会话项内边距: horizontal=16.dp, vertical=12.dp
 * - 删除按钮大小: 36.dp
 * - 删除图标大小: 18.dp
 */
package com.example.androidfronted.ui.smartcustomerservice.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.androidfronted.data.model.smartcustomerservice.ChatSession
import java.text.SimpleDateFormat
import java.util.*

/**
 * 侧边栏主组件
 * 
 * 显示聊天会话列表，支持搜索、切换和删除会话
 * 
 * @param sessions 会话列表
 * @param currentSessionId 当前会话ID
 * @param onSessionSelect 会话选择回调
 * @param onNewSession 新建会话回调
 * @param onDeleteSession 删除会话回调
 * @param modifier 修饰符
 */
@Composable
fun SideDrawer(
    sessions: List<ChatSession>,
    currentSessionId: String?,
    onSessionSelect: (String) -> Unit,
    onNewSession: () -> Unit,
    onDeleteSession: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    
    ModalDrawerSheet(
        modifier = modifier.width(280.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            // 头部：标题和搜索框
            DrawerHeader(
                searchText = searchText,
                onSearchChange = { searchText = it }
            )
            
            // 分隔线
            HorizontalDivider()
            
            // 会话列表
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(sessions.filter { 
                    it.title.contains(searchText, ignoreCase = true) || 
                    it.lastMessage.contains(searchText, ignoreCase = true) 
                }) { session ->
                    SessionItem(
                        session = session,
                        isSelected = session.id == currentSessionId,
                        onSelect = { onSessionSelect(session.id) },
                        onDelete = { onDeleteSession(session.id) }
                    )
                }
            }
        }
    }
}

/**
 * 侧边栏头部组件
 * 
 * 包含标题和搜索框
 * 
 * @param searchText 搜索文本
 * @param onSearchChange 搜索文本变化回调
 */
@Composable
private fun DrawerHeader(
    searchText: String,
    onSearchChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "聊天记录",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 搜索框
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索对话记录") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索"
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

/**
 * 会话列表项组件
 * 
 * 显示单个会话的信息，包括标题、最后消息和时间
 * 
 * @param session 会话数据
 * @param isSelected 是否被选中
 * @param onSelect 选择回调
 * @param onDelete 删除回调
 */
@Composable
private fun SessionItem(
    session: ChatSession,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 会话信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 会话标题
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 最后一条消息
                if (session.lastMessage.isNotEmpty()) {
                    Text(
                        text = session.lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // 时间戳
                Text(
                    text = formatTimestamp(session.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 删除按钮
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 格式化时间戳
 * 
 * 将毫秒时间戳转换为 "MM/dd HH:mm" 格式的字符串
 * 
 * @param timestamp 毫秒时间戳
 * @return 格式化后的时间字符串
 */
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
