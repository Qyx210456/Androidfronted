/**
 * 消息气泡组件
 * 
 * 该文件包含智能客服聊天界面的消息气泡组件，包括：
 * - MessageBubble: 消息气泡主组件
 * - StreamingMessageBubble: 流式消息气泡（打字机效果）
 * - AiAvatar: AI头像组件
 * - UserAvatar: 用户头像组件
 * - MessageActionButtons: 消息操作按钮组
 * - ActionButton: 单个操作按钮
 * 
 * UI尺寸参考：
 * - AI/用户头像大小: 48.dp
 * - 气泡最大宽度: 280.dp
 * - 气泡圆角: topStart=16.dp, topEnd=16.dp, bottomStart/bottomEnd=4.dp或16.dp
 * - 气泡内边距: 12.dp
 * - 操作按钮大小: 32.dp
 * - 操作按钮图标大小: 18.dp
 * - 更多菜单图标大小: 20.dp
 */
package com.example.androidfronted.ui.smartcustomerservice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.androidfronted.R
import com.example.androidfronted.data.model.smartcustomerservice.ChatMessage
import com.example.androidfronted.ui.smartcustomerservice.theme.ChatAiBubble
import com.example.androidfronted.ui.smartcustomerservice.theme.ChatUserBubble
import com.example.androidfronted.utils.ImageUrlHelper

/**
 * 消息气泡主组件
 * 
 * 显示单条聊天消息，包括头像、气泡内容和操作按钮
 * 
 * @param message 消息数据模型
 * @param userAvatarUrl 用户头像URL
 * @param onCopy 复制回调
 * @param onLike 点赞回调
 * @param onDislike 点踩回调
 * @param onRetry 重试回调
 * @param onShare 分享回调
 * @param onFavorite 收藏回调
 * @param onDelete 删除回调
 * @param modifier 修饰符
 */
@Composable
fun MessageBubble(
    message: ChatMessage,
    userAvatarUrl: String? = null,
    onCopy: () -> Unit = {},
    onLike: () -> Unit = {},
    onDislike: () -> Unit = {},
    onRetry: () -> Unit = {},
    onShare: () -> Unit = {},
    onFavorite: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isFromUser = message.isFromUser
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // AI消息显示AI头像在左侧
        if (!isFromUser) {
            AiAvatar(
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        
        Column(
            horizontalAlignment = if (isFromUser) Alignment.End else Alignment.Start
        ) {
            // AI消息显示"智能客服"标签
            if (!isFromUser) {
                Text(
                    text = "智能客服",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            
            // 消息气泡
            Box(
                modifier = Modifier
                    .widthIn(max = if (isFromUser) 280.dp else 340.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isFromUser) 16.dp else 4.dp,
                            bottomEnd = if (isFromUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (isFromUser) ChatUserBubble else ChatAiBubble)
                    .padding(12.dp)
            ) {
                if (isFromUser) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    MarkdownText(
                        markdown = message.content,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        textSize = 16f
                    )
                }
            }
            
            // AI消息显示操作按钮
            if (!isFromUser && !message.isStreaming) {
                MessageActionButtons(
                    onCopy = onCopy,
                    onLike = onLike,
                    onDislike = onDislike,
                    onRetry = onRetry,
                    onShare = onShare,
                    onFavorite = onFavorite,
                    onDelete = onDelete,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        // 用户消息显示用户头像在右侧
        if (isFromUser) {
            UserAvatar(
                avatarUrl = userAvatarUrl,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * 流式消息气泡组件
 * 
 * 用于显示正在输入中的AI消息，实现打字机效果
 * 采用双模式渲染：流式纯文本 + 完成后Markdown渲染
 * 
 * @param content 当前已接收的消息内容
 * @param modifier 修饰符
 */
@Composable
fun StreamingMessageBubble(
    content: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        AiAvatar(
            modifier = Modifier.padding(end = 8.dp)
        )
        
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "智能客服 (正在思考...)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Box(
                modifier = Modifier
                    .widthIn(max = 340.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .background(ChatAiBubble)
                    .padding(12.dp)
            ) {
                MarkdownText(
                    markdown = content,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    textSize = 16f,
                    isStreaming = true
                )
            }
        }
    }
}

/**
 * AI头像组件
 * 
 * 显示智能客服的圆形头像
 * 
 * @param modifier 修饰符
 */
@Composable
private fun AiAvatar(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_smart_customer_service_avatar),
            contentDescription = "AI头像",
            modifier = Modifier.fillMaxSize(),
            tint = Color.Unspecified
        )
    }
}

/**
 * 用户头像组件
 * 
 * 显示用户的圆形头像，支持网络图片和默认头像
 * 
 * @param avatarUrl 用户头像URL
 * @param modifier 修饰符
 */
@Composable
private fun UserAvatar(
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    val fullUrl = if (!avatarUrl.isNullOrEmpty()) {
        ImageUrlHelper.getFullImageUrl(avatarUrl)
    } else {
        null
    }
    
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (!fullUrl.isNullOrEmpty()) {
            // 使用网络图片
            AsyncImage(
                model = fullUrl,
                contentDescription = "用户头像",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.user_avatar_test),
                placeholder = painterResource(id = R.drawable.user_avatar_test)
            )
        } else {
            // 使用默认头像图标
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "用户头像",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/**
 * 消息操作按钮组组件
 * 
 * 显示复制、点赞、点踩、重试和更多操作按钮
 * 
 * @param onCopy 复制回调
 * @param onLike 点赞回调
 * @param onDislike 点踩回调
 * @param onRetry 重试回调
 * @param onShare 分享回调
 * @param onFavorite 收藏回调
 * @param onDelete 删除回调
 * @param modifier 修饰符
 */
@Composable
private fun MessageActionButtons(
    onCopy: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onRetry: () -> Unit,
    onShare: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 复制按钮
        ActionButton(
            iconRes = R.drawable.ic_smart_customer_service_copy,
            contentDescription = "复制",
            onClick = onCopy
        )
        // 点赞按钮
        ActionButton(
            iconRes = R.drawable.ic_smart_customer_service_like,
            contentDescription = "点赞",
            onClick = onLike
        )
        // 点踩按钮
        ActionButton(
            iconRes = R.drawable.ic_smart_customer_service_dislike,
            contentDescription = "点踩",
            onClick = onDislike
        )
        // 重试按钮
        ActionButton(
            iconRes = R.drawable.ic_smart_customer_service_regenerate,
            contentDescription = "重试",
            onClick = onRetry
        )
        
        // 更多按钮和下拉菜单
        Box {
            ActionButton(
                iconRes = R.drawable.ic_smart_customer_service_more,
                contentDescription = "更多",
                onClick = { showMoreMenu = true }
            )
            
            // 更多操作下拉菜单
            DropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = { showMoreMenu = false },
                modifier = Modifier.wrapContentSize(),
                offset = DpOffset(0.dp, 0.dp)
            ) {
                // 分享选项
                DropdownMenuItem(
                    text = { Text("分享", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_smart_customer_service_share),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        showMoreMenu = false
                        onShare()
                    }
                )
                // 收藏选项
                DropdownMenuItem(
                    text = { Text("收藏", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_smart_customer_service_favorite),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        showMoreMenu = false
                        onFavorite()
                    }
                )
                // 删除选项
                DropdownMenuItem(
                    text = { Text("删除", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_smart_customer_service_delete),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        showMoreMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

/**
 * 操作按钮组件
 * 
 * 单个操作按钮，包含图标
 * 
 * @param iconRes 图标资源ID
 * @param contentDescription 内容描述
 * @param onClick 点击回调
 */
@Composable
private fun ActionButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
