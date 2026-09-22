/**
 * 智能客服主屏幕组件
 *
 * 该文件包含智能客服聊天界面的主屏幕组件，包括：
 * - SmartCustomerServiceScreen: 主屏幕组件
 * - WelcomeContent: 欢迎内容组件（显示流式欢迎消息）
 * - CenteredTopAppBar: 居中标题的顶部栏组件
 *
 * 功能说明：
 * 1. 显示聊天消息列表（支持历史消息向上分页，保持滚动位置）
 * 2. 处理用户输入和消息发送（打字机流式效果）
 * 3. 管理侧边栏的打开和关闭（会话列表管理：重命名/置顶/多选删除）
 * 4. 键盘弹出时输入区域上移且消息列表滚到最新
 * 5. 底部输入区域白色背景延伸覆盖导航条区域
 */
package com.example.androidfronted.ui.smartcustomerservice.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
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
private val BottomWhite = Color.White

/**
 * 智能客服主屏幕组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCustomerServiceScreen(
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    startDrawerOpen: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    SmartCustomerServiceTheme {
        // 从ViewModel收集状态
        val messages by viewModel.messages.collectAsStateWithLifecycle()
        val inputText by viewModel.inputText.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val chatSessions by viewModel.chatSessions.collectAsStateWithLifecycle()
        val sessionsHasMore by viewModel.sessionsHasMore.collectAsStateWithLifecycle()
        val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
        val currentStreamingMessage by viewModel.currentStreamingMessage.collectAsStateWithLifecycle()
        val isStreaming by viewModel.isStreaming.collectAsStateWithLifecycle()
        val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
        val userAvatar by viewModel.userAvatar.collectAsStateWithLifecycle()
        val welcomeMessage by viewModel.welcomeMessage.collectAsStateWithLifecycle()
        val isWelcomeStreaming by viewModel.isWelcomeStreaming.collectAsStateWithLifecycle()
        val showWelcome by viewModel.showWelcome.collectAsStateWithLifecycle()
        val isLoadingOlder by viewModel.isLoadingOlder.collectAsStateWithLifecycle()

        // UI状态（startDrawerOpen：从搜索页返回时侧边栏保持展开）
        var drawerOpen by remember { mutableStateOf(startDrawerOpen) }
        var inputMode by remember { mutableStateOf(InputMode.KEYBOARD) }
        var showTools by remember { mutableStateOf(false) }
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        // 是否滚动在最新消息附近（离开底部时显示"回到底部"按钮）
        val isAtBottom by remember {
            derivedStateOf {
                val info = listState.layoutInfo
                val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
                info.totalItemsCount == 0 || lastVisible >= info.totalItemsCount - 2
            }
        }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        // 键盘可见性：弹出时滚动到最新消息
        val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)

        // 复制消息到剪贴板
        fun copyMessage(message: ChatMessage) {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("message", message.content)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
        }

        // 消息数量变化时的滚动策略：
        // - 历史前置加载（快照存在）→ 保持当前视口位置不跳动
        // - 新消息 → 滚动到最新
        var prevMessageSize by remember { mutableStateOf(0) }
        var viewportSnapshot by remember { mutableStateOf<Pair<Int, Int>?>(null) }

        LaunchedEffect(isLoadingOlder) {
            if (isLoadingOlder && messages.isNotEmpty()) {
                viewportSnapshot =
                    listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            }
        }

        LaunchedEffect(messages.size) {
            val added = messages.size - prevMessageSize
            val snapshot = viewportSnapshot
            if (added > 0 && snapshot != null) {
                // 前置插入更早消息：同一条目整体下移 added，保持视口不动
                listState.scrollToItem((snapshot.first + added).coerceAtMost(messages.size - 1), snapshot.second)
            } else if (added > 0) {
                val totalItems = messages.size +
                    (if (isStreaming && currentStreamingMessage.isNotEmpty()) 1 else 0) + 1
                listState.animateScrollToItem(maxOf(0, totalItems - 1))
            }
            viewportSnapshot = null
            prevMessageSize = messages.size
        }

        // 流式输出时持续滚动到底部
        LaunchedEffect(currentStreamingMessage) {
            if (isStreaming && currentStreamingMessage.isNotEmpty()) {
                val totalItems = messages.size + 1 + 1
                listState.animateScrollToItem(maxOf(0, totalItems - 1))
            }
        }

        // 键盘弹出 → 收起更多功能面板 + 滚动到最新消息
        // 防抖：仅键盘从收起变为弹起时触发；避免收起动画中间帧误触发
        // （如键盘弹起时点"更多功能"：收键盘的同时展开面板，不应被收回）
        var prevImeBottom by remember { mutableStateOf(0) }
        LaunchedEffect(imeBottom) {
            if (imeBottom > 0 && prevImeBottom == 0) {
                if (showTools) showTools = false
                if (messages.isNotEmpty() || currentStreamingMessage.isNotEmpty()) {
                    val totalItems = messages.size +
                        (if (isStreaming && currentStreamingMessage.isNotEmpty()) 1 else 0) + 1
                    listState.animateScrollToItem(maxOf(0, totalItems - 1))
                }
            }
            prevImeBottom = imeBottom
        }

        // 滚动到顶部附近时加载更早历史
        LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
            val nearTop = listState.firstVisibleItemIndex == 0 &&
                listState.firstVisibleItemScrollOffset < 40
            if (nearTop && !isLoadingOlder && messages.isNotEmpty() && !isStreaming) {
                viewModel.loadOlderMessages()
            }
        }

        // 同步drawerOpen状态和drawerState；打开时刷新会话列表
        LaunchedEffect(drawerOpen) {
            if (drawerOpen) {
                viewModel.loadSessions()
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
                    drawerOpen = drawerOpen,
                    onSessionSelect = { sessionId ->
                        viewModel.switchSession(sessionId)
                        drawerOpen = false
                    },
                    onDeleteSession = { sessionId ->
                        viewModel.deleteSession(sessionId)
                    },
                    onRenameSession = { sessionId, title ->
                        viewModel.renameSession(sessionId, title)
                    },
                    onTogglePin = { sessionId, pinned ->
                        viewModel.togglePin(sessionId, pinned)
                    },
                    onDeleteSessions = { ids ->
                        viewModel.deleteSessions(ids)
                    },
                    onPinSessions = { ids, pinned ->
                        viewModel.pinSessions(ids, pinned)
                    },
                    onSearchClick = onSearchClick,
                    hasMore = sessionsHasMore,
                    onLoadMore = { viewModel.loadMoreSessions() }
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
                            if (isLoadingOlder) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = TopBarColor,
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }

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

                    // 回到底部按钮：上滑离开最新消息时出现
                    if (!isAtBottom && (messages.isNotEmpty() || currentStreamingMessage.isNotEmpty())) {
                        Icon(
                            painter = painterResource(R.drawable.smart_customer_server_back_to_bottom),
                            contentDescription = "回到底部",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 16.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFEEEEEE), CircleShape)
                                .clickable {
                                    val totalItems = messages.size +
                                        (if (isStreaming && currentStreamingMessage.isNotEmpty()) 1 else 0) + 1
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(maxOf(0, totalItems - 1))
                                    }
                                }
                        )
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

                // 底部输入区域：白色背景延伸覆盖导航条与底部留白
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BottomWhite)
                        .navigationBarsPadding()
                ) {
                    InputArea(
                        inputText = inputText,
                        isLoading = isLoading,
                        inputMode = inputMode,
                        showTools = showTools,
                        onInputChange = { viewModel.updateInputText(it) },
                        onSendClick = { viewModel.sendMessage() },
                        onInputModeChange = { inputMode = it },
                        onCameraClick = { },
                        onToggleTools = { showTools = !showTools },
                        onPhotoClick = { },
                        onGalleryClick = { },
                        onFileClick = { }
                    )
                }
            }
        }
    }
}

/**
 * 居中标题的顶部栏组件
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
