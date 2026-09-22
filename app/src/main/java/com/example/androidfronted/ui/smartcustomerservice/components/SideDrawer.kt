/**
 * 侧边栏组件（对齐 UI交互逻辑.txt）
 *
 * - 普通模式：白底 + 搜索框+ 会话列表（置顶区与普通区细线分隔，
 *   普通区按"今天/7天内/更早之前"分组，时间出现在对话上方）
 * - 长按单个会话：弹出操作菜单（重命名/置顶/多选/删除-红），删除需二次确认
 * - 多选模式：顶部"选择对话/已选择 X 个对话"+右上角 X 取消；列表项左侧勾选框；
 *   底部"置顶/删除"工具栏（未选中置灰禁用；全部选中项均已置顶时显示"取消置顶"）
 * - 分页：列表滚动接近底部时回调 onLoadMore
 */
package com.example.androidfronted.ui.smartcustomerservice.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.androidfronted.R
import com.example.androidfronted.data.model.smartcustomerservice.ChatSession
import java.text.SimpleDateFormat
import java.util.*

private val DrawerWhite = Color.White
private val DrawerTextPrimary = Color(0xFF222222)
private val DrawerTextSecondary = Color(0xFF666666)
private val DrawerTextTertiary = Color(0xFF999999)
private val DrawerDivider = Color(0xFFDDDDDD)
private val DrawerSelectedBg = Color(0xFFE8F1FF)
private val DrawerCheckSelectedBg = Color(0xFFEAF3FF)
private val DrawerAccentBlue = Color(0xFF458FFC)
private val DrawerDeleteRed = Color(0xFFE53935)

/** 无涟漪点击 */
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

/** 长按菜单项：图标 + 文本（白色 Surface 内自绘行） */
@Composable
private fun PopupMenuItem(
    iconRes: Int,
    label: String,
    labelColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (labelColor == DrawerDeleteRed) Color.Unspecified else DrawerTextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, color = labelColor)
    }
}

/** 抽屉列表行类型 */
private sealed class DrawerRow {
    /** showMultiSelect：该分组行右侧显示"多选"入口图标（仅普通区第一个分组） */
    data class Group(val title: String, val showMultiSelect: Boolean = false) : DrawerRow()
    data class Session(val session: ChatSession) : DrawerRow()
    object Divider : DrawerRow()
}

/** 时间分组：今天 / 7天内 / 更早之前 */
private fun timeGroupOf(timestamp: Long): String {
    val now = System.currentTimeMillis()
    return when {
        isSameDay(timestamp, now) -> "今天"
        timestamp > now - 7L * 24 * 60 * 60 * 1000 -> "7天内"
        else -> "更早之前"
    }
}

private fun isSameDay(a: Long, b: Long): Boolean {
    val ca = Calendar.getInstance().apply { timeInMillis = a }
    val cb = Calendar.getInstance().apply { timeInMillis = b }
    return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
        ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
}

/** 构建置顶区 + 分组分隔的扁平行列表（保持服务器排序） */
private fun buildDrawerRows(sessions: List<ChatSession>, query: String): List<DrawerRow> {
    val filtered = if (query.isBlank()) sessions else sessions.filter {
        it.title.contains(query, ignoreCase = true) || it.lastMessage.contains(query, ignoreCase = true)
    }
    val pinned = filtered.filter { it.pinned }
    val normal = filtered.filter { !it.pinned }

    val rows = mutableListOf<DrawerRow>()
    if (pinned.isNotEmpty()) {
        rows.add(DrawerRow.Group("置顶"))
        pinned.forEach { rows.add(DrawerRow.Session(it)) }
    }
    if (pinned.isNotEmpty() && normal.isNotEmpty()) rows.add(DrawerRow.Divider)

    var lastGroup: String? = null
    normal.forEach { session ->
        val group = timeGroupOf(session.timestamp)
        if (group != lastGroup) {
            rows.add(DrawerRow.Group(group))
            lastGroup = group
        }
        rows.add(DrawerRow.Session(session))
    }

    // 多选入口固定显示在列表第一个分组行右侧（置顶区或时间区），不随时间分组标题变化
    val firstGroupIndex = rows.indexOfFirst { it is DrawerRow.Group }
    if (firstGroupIndex >= 0) {
        rows[firstGroupIndex] =
            (rows[firstGroupIndex] as DrawerRow.Group).copy(showMultiSelect = true)
    }
    return rows
}

/**
 * 侧边栏主组件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SideDrawer(
    sessions: List<ChatSession>,
    currentSessionId: String?,
    drawerOpen: Boolean,
    onSessionSelect: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onRenameSession: (String, String) -> Unit,
    onTogglePin: (String, Boolean) -> Unit,
    onDeleteSessions: (List<String>) -> Unit,
    onPinSessions: (List<String>, Boolean) -> Unit,
    onSearchClick: () -> Unit,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var multiSelectMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    var menuSessionId by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<Set<String>?>(null) }
    var renameTarget by remember { mutableStateOf<ChatSession?>(null) }

    val listState = rememberLazyListState()

    // 抽屉关闭时退出多选模式并清空选中
    LaunchedEffect(drawerOpen) {
        if (!drawerOpen && multiSelectMode) {
            multiSelectMode = false
            selectedIds = emptySet()
        }
    }

    // 滚动接近底部时加载更多
    val shouldLoadMore by remember {
        derivedStateOf {
            if (!hasMore) return@derivedStateOf false
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    val rows = remember(sessions) { buildDrawerRows(sessions, "") }

    // 多选模式底部按钮状态
    val hasSelection = selectedIds.isNotEmpty()
    val allSelectedPinned = hasSelection && sessions
        .filter { it.id in selectedIds }
        .all { it.pinned }

    ModalDrawerSheet(
        modifier = modifier.width(300.dp),
        drawerContainerColor = DrawerWhite
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            if (multiSelectMode) {
                // ---------- 多选模式头部：标题动态 + 右上角 X 取消 ----------
                MultiSelectHeader(
                    selectedCount = selectedIds.size,
                    onCancel = {
                        multiSelectMode = false
                        selectedIds = emptySet()
                    }
                )
            } else {
                // ---------- 普通模式头部：仅搜索框（点击跳转搜索页） ----------
                DrawerHeader(onClick = onSearchClick)
            }

            HorizontalDivider(color = DrawerDivider)

            // ---------- 会话列表 ----------
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f)
            ) {
                items(rows, key = { row ->
                    when (row) {
                        is DrawerRow.Session -> "s_${row.session.id}"
                        is DrawerRow.Group -> "g_${row.title}"
                        DrawerRow.Divider -> "divider"
                    }
                }) { row ->
                    when (row) {
                        is DrawerRow.Group -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = row.title,
                                    fontSize = 14.sp,
                                    color = DrawerTextSecondary
                                )
                                // 分组行右侧的多选入口（仅普通模式第一个时间分组）
                                if (row.showMultiSelect && !multiSelectMode) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        painter = painterResource(R.drawable.smart_customer_server_multi_select),
                                        contentDescription = "多选",
                                        tint = DrawerTextSecondary,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickableNoRipple { multiSelectMode = true }
                                    )
                                }
                            }
                        }
                        DrawerRow.Divider -> {
                            HorizontalDivider(
                                color = DrawerDivider,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        is DrawerRow.Session -> {
                            val session = row.session
                            var itemHeightPx by remember { mutableIntStateOf(0) }
                            // 菜单右缘与侧边栏右缘的间距
                            val menuOffsetXPx = with(LocalDensity.current) { 12.dp.roundToPx() }
                            Box(modifier = Modifier.onSizeChanged { itemHeightPx = it.height }) {
                                SessionItem(
                                    session = session,
                                    isSelected = session.id == currentSessionId,
                                    multiSelectMode = multiSelectMode,
                                    checked = session.id in selectedIds,
                                    onSelect = {
                                        if (multiSelectMode) {
                                            selectedIds =
                                                if (session.id in selectedIds) selectedIds - session.id
                                                else selectedIds + session.id
                                        } else {
                                            onSessionSelect(session.id)
                                        }
                                    },
                                    onLongPress = {
                                        if (!multiSelectMode) menuSessionId = session.id
                                    }
                                )
                                // 长按操作菜单：出现在对话右下角（顶缘=对话底缘，右缘对齐），白色弥散阴影
                                if (menuSessionId == session.id && !multiSelectMode) {
                                    Popup(
                                        alignment = Alignment.TopEnd,
                                        offset = IntOffset(-menuOffsetXPx, itemHeightPx),
                                        onDismissRequest = { menuSessionId = null },
                                        properties = PopupProperties(focusable = true)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = DrawerWhite,
                                            shadowElevation = 8.dp
                                        ) {
                                            Column(modifier = Modifier.width(160.dp)) {
                                                PopupMenuItem(
                                                    iconRes = R.drawable.smart_customer_server_rename,
                                                    label = "重命名",
                                                    labelColor = DrawerTextPrimary
                                                ) {
                                                    menuSessionId = null
                                                    renameTarget = session
                                                }
                                                PopupMenuItem(
                                                    iconRes = if (session.pinned) {
                                                        R.drawable.smart_customer_server_unpin
                                                    } else {
                                                        R.drawable.smart_customer_server_pin_filled
                                                    },
                                                    label = if (session.pinned) "取消置顶" else "置顶",
                                                    labelColor = DrawerTextPrimary
                                                ) {
                                                    menuSessionId = null
                                                    onTogglePin(session.id, !session.pinned)
                                                }
                                                PopupMenuItem(
                                                    iconRes = R.drawable.smart_customer_server_multi_select,
                                                    label = "多选",
                                                    labelColor = DrawerTextPrimary
                                                ) {
                                                    menuSessionId = null
                                                    multiSelectMode = true
                                                    selectedIds = setOf(session.id)
                                                }
                                                HorizontalDivider(color = DrawerDivider)
                                                PopupMenuItem(
                                                    iconRes = R.drawable.smart_customer_server_trash_filled_red,
                                                    label = "删除",
                                                    labelColor = DrawerDeleteRed
                                                ) {
                                                    menuSessionId = null
                                                    deleteTarget = setOf(session.id)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ---------- 多选模式底部工具栏 ----------
            if (multiSelectMode) {
                MultiSelectBottomBar(
                    enabled = hasSelection,
                    showUnpin = allSelectedPinned,
                    onPin = {
                        onPinSessions(selectedIds.toList(), !allSelectedPinned)
                        multiSelectMode = false
                        selectedIds = emptySet()
                    },
                    onDelete = {
                        deleteTarget = selectedIds.toSet()
                    }
                )
            }
        }
    }

    // ---------- 删除二次确认 ----------
    deleteTarget?.let { targets ->
        DeleteConfirmDialog(
            message = if (targets.size == 1) "确定要删除该对话吗？" else "确定要删除选中的 ${targets.size} 个对话吗？",
            onConfirm = {
                if (targets.size == 1) onDeleteSession(targets.first())
                else onDeleteSessions(targets.toList())
                deleteTarget = null
                multiSelectMode = false
                selectedIds = emptySet()
            },
            onDismiss = { deleteTarget = null }
        )
    }

    // ---------- 重命名 ----------
    renameTarget?.let { target ->
        RenameDialog(
            initialTitle = target.title,
            onConfirm = { newTitle ->
                onRenameSession(target.id, newTitle)
                renameTarget = null
            },
            onDismiss = { renameTarget = null }
        )
    }
}

/**
 * 多选模式头部：标题动态显示数量 + 右上角 X 取消
 */
@Composable
private fun MultiSelectHeader(
    selectedCount: Int,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (selectedCount == 0) "选择对话" else "已选择 $selectedCount 个对话",
            fontSize = 17.sp,
            color = DrawerTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(R.drawable.smart_customer_server_close_x),
            contentDescription = null,
            tint = DrawerTextSecondary,
            modifier = Modifier
                .size(28.dp)
                .clickableNoRipple(onCancel)
        )
    }
}

/**
 * 多选模式底部工具栏：置顶 / 删除（未选中时置灰禁用）
 */
@Composable
private fun MultiSelectBottomBar(
    enabled: Boolean,
    showUnpin: Boolean,
    onPin: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(color = DrawerWhite) {
        Column {
            HorizontalDivider(color = DrawerDivider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
            // 置顶 / 取消置顶
            Row(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (enabled) 1f else 0.4f)
                    .clickableNoRipple { if (enabled) onPin() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(
                        if (showUnpin) R.drawable.smart_customer_server_unpin
                        else R.drawable.smart_customer_server_pin_outline
                    ),
                    contentDescription = null,
                    tint = if (enabled) DrawerTextPrimary else DrawerTextTertiary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showUnpin) "取消置顶" else "置顶",
                    fontSize = 18.sp,
                    color = if (enabled) DrawerTextPrimary else DrawerTextTertiary
                )
            }
            // 删除（红色高亮）
            Row(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (enabled) 1f else 0.4f)
                    .clickableNoRipple { if (enabled) onDelete() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.smart_customer_server_trash_filled_red),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "删除",
                    fontSize = 18.sp,
                    color = DrawerDeleteRed
                )
            }
            }
        }
    }
}

/** 抽屉顶部搜索框：只读样式，点击跳转独立搜索页 */
@Composable
private fun DrawerHeader(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF7F8FA))
            .border(1.dp, DrawerDivider, RoundedCornerShape(24.dp))
            .clickableNoRipple(onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "搜索",
            tint = DrawerTextTertiary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "搜索对话记录",
            fontSize = 14.sp,
            color = DrawerTextTertiary
        )
    }
}

/**
 * 会话列表项：普通模式（点击切换/长按菜单）与多选模式（勾选框）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SessionItem(
    session: ChatSession,
    isSelected: Boolean,
    multiSelectMode: Boolean,
    checked: Boolean,
    onSelect: () -> Unit,
    onLongPress: () -> Unit
) {
    val backgroundColor = when {
        multiSelectMode && checked -> DrawerCheckSelectedBg
        // 多选模式下取消"当前会话标蓝"，仅保留勾选高亮
        isSelected && !multiSelectMode -> DrawerSelectedBg
        else -> DrawerWhite
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect,
                onLongClick = onLongPress
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 多选模式勾选框
        if (multiSelectMode) {
            Icon(
                painter = painterResource(
                    if (checked) R.drawable.smart_customer_server_checkbox_checked
                    else R.drawable.smart_customer_server_checkbox_empty
                ),
                contentDescription = if (checked) "已选中" else "未选中",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(22.dp)
                    .padding(end = 0.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        // 会话信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.title,
                fontSize = 16.sp,
                color = DrawerTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (session.lastMessage.isNotEmpty()) {
                Text(
                    text = session.lastMessage,
                    fontSize = 14.sp,
                    color = DrawerTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = formatTimestamp(session.timestamp),
                fontSize = 12.sp,
                color = DrawerTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** 毫秒时间戳 → "MM/dd HH:mm" */
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
