/**
 * 智能客服主屏幕组件
 * 
 * 该文件包含智能客服聊天界面的主屏幕组件，包括：
 * - SmartCustomerServiceScreen: 主屏幕组件
 * - WelcomeContent: 欢迎内容组件（显示流式欢迎消息）
 * - CenteredTopAppBar: 居中标题的顶部栏组件
 * 
 * 功能说明：
 * 1. 显示聊天消息列表
 * 2. 处理用户输入和消息发送
 * 3. 管理侧边栏的打开和关闭
 * 4. 显示流式消息和欢迎消息
 * 5. 处理错误消息显示
 * 6. 键盘弹出时输入区域上移
 * 
 * UI尺寸参考：
 * - 顶部栏高度: 56.dp (不含状态栏)
 * - 顶部栏颜色: #458FFC
 * - 图标按钮大小: 44.dp
 * - 图标大小: 24.dp
 * - 欢迎头像大小: 80.dp
 * - 内容内边距: 24.dp
 */
package com.example.androidfronted.ui.smartcustomerservice.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.androidfronted.R
import com.example.androidfronted.data.model.smartcustomerservice.ChatMessage
import com.example.androidfronted.ui.smartcustomerservice.components.InputArea
import com.example.androidfronted.ui.smartcustomerservice.components.InputMode
import com.example.androidfronted.ui.smartcustomerservice.components.MessageBubble
import com.example.androidfronted.ui.smartcustomerservice.components.SideDrawer
import com.example.androidfronted.ui.smartcustomerservice.components.StreamingMessageBubble
import com.example.androidfronted.ui.smartcustomerservice.theme.ChatBackground
import com.example.androidfronted.ui.smartcustomerservice.theme.SmartCustomerServiceTheme
import com.example.androidfronted.viewmodel.smartcustomerservice.ChatViewModel

private val TopBarColor = Color(0xFF458FFC)

/**
 * 智能客服主屏幕组件
 * 
 * 包含顶部栏、消息列表、输入区域和侧边栏
 * 
 * @param viewModel 聊天ViewModel
 * @param onBackClick 返回按钮点击回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCustomerServiceScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    SmartCustomerServiceTheme {
        // 从ViewModel收集状态
        val messages by viewModel.messages.collectAsStateWithLifecycle()
        val inputText by viewModel.inputText.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val chatSessions by viewModel.chatSessions.collectAsStateWithLifecycle()
        val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
        val currentStreamingMessage by viewModel.currentStreamingMessage.collectAsStateWithLifecycle()
        val isStreaming by viewModel.isStreaming.collectAsStateWithLifecycle()
        val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
        val userAvatar by viewModel.userAvatar.collectAsStateWithLifecycle()
        val welcomeMessage by viewModel.welcomeMessage.collectAsStateWithLifecycle()
        val isWelcomeStreaming by viewModel.isWelcomeStreaming.collectAsStateWithLifecycle()
        val showWelcome by viewModel.showWelcome.collectAsStateWithLifecycle()
        
        // UI状态
        var drawerOpen by remember { mutableStateOf(false) }
        var inputMode by remember { mutableStateOf(InputMode.KEYBOARD) }
        var showTools by remember { mutableStateOf(false) }
        val listState = rememberLazyListState()
        
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        
        // 复制消息到剪贴板
        fun copyMessage(message: ChatMessage) {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("message", message.content)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
        }
        
        // 自动滚动到最新消息
        LaunchedEffect(messages.size, currentStreamingMessage) {
            if (messages.isNotEmpty() || currentStreamingMessage.isNotEmpty()) {
                val totalItems = messages.size +
                    (if (isStreaming && currentStreamingMessage.isNotEmpty()) 1 else 0) + 1
                listState.animateScrollToItem(maxOf(0, totalItems - 1))
            }
        }
        
        // 同步drawerOpen状态和drawerState
        LaunchedEffect(drawerOpen) {
            if (drawerOpen) {
                drawerState.open()
            } else {
                drawerState.close()
            }
        }
        
        // 当抽屉关闭时更新drawerOpen状态并收起键盘
        LaunchedEffect(drawerState.currentValue) {
            if (drawerState.currentValue == DrawerValue.Closed) {
                drawerOpen = false
                val keyboardController = context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                keyboardController.hideSoftInputFromWindow((context as android.app.Activity).currentFocus?.windowToken, 0)
            }
        }
        
        // 自动清除错误消息
        errorMessage?.let { error ->
            LaunchedEffect(error) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearError()
            }
        }
        
        // 侧边栏导航抽屉
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerOpen,
            scrimColor = if (drawerOpen) Color.Black.copy(alpha = 0.5f) else Color.Transparent,
            drawerContent = {
                SideDrawer(
                    sessions = chatSessions,
                    currentSessionId = currentSessionId,
                    onSessionSelect = { sessionId ->
                        viewModel.switchSession(sessionId)
                        drawerOpen = false
                    },
                    onNewSession = {
                        viewModel.createNewSession()
                        drawerOpen = false
                    },
                    onDeleteSession = { sessionId ->
                        viewModel.deleteSession(sessionId)
                    }
                )
            }
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .then(
                        if (drawerOpen) Modifier else Modifier.imePadding()
                    )
            ) {
                // 顶部导航栏（包含状态栏区域）
                CenteredTopAppBar(
                    title = "智能客服",
                    onBackClick = onBackClick,
                    onMenuClick = { drawerOpen = true },
                    onNewChatClick = { viewModel.createNewSession() }
                )
                
                // 消息列表区域
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(ChatBackground)
                ) {
                    // 显示欢迎内容
                    if (showWelcome && messages.isEmpty() && !isStreaming) {
                        WelcomeContent(
                            welcomeMessage = welcomeMessage,
                            isStreaming = isWelcomeStreaming,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } 
                    // 显示消息列表
                    else if (messages.isNotEmpty() || isStreaming) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(messages) { message ->
                                MessageBubble(
                                    message = message,
                                    userAvatarUrl = userAvatar,
                                    onCopy = { copyMessage(message) },
                                    onLike = { },
                                    onDislike = { },
                                    onRetry = { },
                                    onShare = { },
                                    onFavorite = { },
                                    onDelete = { }
                                )
                            }
                            
                            // 显示流式消息
                            if (isStreaming && currentStreamingMessage.isNotEmpty()) {
                                item {
                                    StreamingMessageBubble(content = currentStreamingMessage)
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(1.dp))
                            }
                        }
                    }
                    
                    // 显示错误消息
                    errorMessage?.let { error ->
                        Snackbar(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            action = {
                                TextButton(onClick = { viewModel.clearError() }) {
                                    Text("关闭")
                                }
                            }
                        ) {
                            Text(error)
                        }
                    }
                }
                
                // 输入区域
                InputArea(
                    inputText = inputText,
                    isLoading = isLoading,
                    inputMode = inputMode,
                    showTools = showTools,
                    onInputChange = { viewModel.updateInputText(it) },
                    onSendClick = { viewModel.sendMessage() },
                    onCancelClick = { },
                    onInputModeChange = { inputMode = it },
                    onCameraClick = { },
                    onToggleTools = { showTools = !showTools },
                    onPhotoClick = { },
                    onGalleryClick = { },
                    onFileClick = { },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    }
}

/**
 * 居中标题的顶部栏组件
 * 
 * 实现以下特性：
 * 1. 蓝色背景包含状态栏和Toolbar区域
 * 2. Toolbar高度为56dp（与XML布局一致）
 * 3. 文字绝对居中（通过左右对称占位实现）
 * 4. 图标点击有按压反馈
 * 
 * @param title 标题文字
 * @param onBackClick 返回按钮点击回调
 * @param onMenuClick 菜单按钮点击回调
 * @param onNewChatClick 新建对话按钮点击回调
 */
@Composable
private fun CenteredTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TopBarColor)
    ) {
        // 状态栏区域 - 让蓝色背景延伸到状态栏
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        )
        
        // Toolbar区域 - 高度56dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            // 标题文字 - 绝对居中
            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                color = Color.White
            )
            
            // 左侧图标区域
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 返回按钮
                PressableIconButton(
                    onClick = onBackClick,
                    iconRes = R.drawable.ic_arrow_back_white,
                    contentDescription = "返回"
                )
                // 菜单按钮
                PressableIconButton(
                    onClick = onMenuClick,
                    iconRes = R.drawable.ic_smart_customer_service_expand_right,
                    contentDescription = "打开菜单"
                )
            }
            
            // 右侧图标区域 - 与左侧对称占位
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 透明占位（与左侧菜单按钮对称）
                Box(
                    modifier = Modifier.size(44.dp)
                )
                // 新建对话按钮
                PressableIconButton(
                    onClick = onNewChatClick,
                    iconRes = R.drawable.ic_smart_customer_service_new_chat,
                    contentDescription = "新建对话"
                )
            }
        }
    }
}

/**
 * 可按压的图标按钮组件
 * 
 * 带有按压反馈效果的图标按钮
 * 
 * @param onClick 点击回调
 * @param iconRes 图标资源ID
 * @param contentDescription 内容描述
 */
@Composable
private fun PressableIconButton(
    onClick: () -> Unit,
    iconRes: Int,
    contentDescription: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale = if (isPressed) 0.85f else 1f
    val alpha = if (isPressed) 0.7f else 1f
    
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        )
    }
}

/**
 * 欢迎内容组件
 * 
 * 显示流式欢迎消息和AI头像
 * 
 * @param welcomeMessage 欢迎消息内容
 * @param isStreaming 是否正在流式输出
 * @param modifier 修饰符
 */
@Composable
private fun WelcomeContent(
    welcomeMessage: String,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // AI头像
        Icon(
            painter = painterResource(id = R.drawable.ic_smart_customer_service_avatar),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            tint = Color.Unspecified
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 欢迎消息（带光标）
        if (welcomeMessage.isNotEmpty()) {
            Text(
                text = if (isStreaming) "$welcomeMessage▌" else welcomeMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        } else if (isStreaming) {
            // 加载指示器
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = TopBarColor
            )
        }
    }
}
