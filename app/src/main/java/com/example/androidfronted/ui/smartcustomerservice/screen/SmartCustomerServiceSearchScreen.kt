/**
 * 智能客服搜索页
 *
 * - 顶部：返回键 + 搜索框（进入自动聚焦弹出键盘）
 * - 未输入内容：仅显示顶部区域
 * - 输入内容：结果匹配逻辑暂未实现，显示占位提示
 */
package com.example.androidfronted.ui.smartcustomerservice.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SearchPageBg = Color.White
private val SearchTextPrimary = Color(0xFF222222)
private val SearchTextTertiary = Color(0xFF999999)
private val SearchFieldBg = Color(0xFFF7F8FA)
private val SearchDivider = Color(0xFFEEEEEE)
private val SearchAccentBlue = Color(0xFF458FFC)

@Composable
fun SmartCustomerServiceSearchScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // 进入页面自动聚焦搜索框
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(modifier = modifier.fillMaxSize(), color = SearchPageBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部：返回键 + 搜索框
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = SearchTextPrimary
                    )
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                        .focusRequester(focusRequester),
                    placeholder = { Text("搜索对话记录", fontSize = 14.sp, color = SearchTextTertiary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = SearchTextTertiary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SearchFieldBg,
                        unfocusedContainerColor = SearchFieldBg,
                        focusedBorderColor = SearchAccentBlue.copy(alpha = 0.4f),
                        unfocusedBorderColor = SearchDivider
                    )
                )
            }

            HorizontalDivider(color = SearchDivider)

            // 结果区（匹配逻辑暂未实现）
            if (query.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "暂无相关搜索结果",
                        fontSize = 14.sp,
                        color = SearchTextTertiary
                    )
                }
            }
        }
    }
}
