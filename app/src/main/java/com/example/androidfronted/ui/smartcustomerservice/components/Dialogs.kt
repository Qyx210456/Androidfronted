/**
 * 智能客服对话框组件
 *
 * - DeleteConfirmDialog: 删除会话二次确认弹窗：
 *   图标 + 提示文字居中 + 灰色"取消"/蓝色渐变"确定"双按钮）
 * - RenameDialog: 会话重命名对话框
 */
package com.example.androidfronted.ui.smartcustomerservice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidfronted.R

/** 无按压涟漪的点击修饰符 */
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

/** 取消贷款申请弹窗同款配色 */
private val ConfirmGray = Color(0xFFE6E6E6)
private val ConfirmTextSecondary = Color(0xFF666666)
private val ConfirmGradient = Brush.horizontalGradient(
    listOf(Color(0xFF458FFC), Color(0xFF2D7FF9))
)

/**
 * 删除会话二次确认弹窗
 *
 * @param message 提示文案（如"确定要删除该对话吗？"）
 * @param onConfirm 确认删除
 * @param onDismiss 取消
 */
@Composable
fun DeleteConfirmDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = null,
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_smart_customer_service_delete),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = ConfirmTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 取消按钮（灰色）
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(ConfirmGray)
                            .clickableNoRipple { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "取消",
                            fontSize = 16.sp,
                            color = ConfirmTextSecondary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                    // 确定按钮（蓝色渐变）
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(ConfirmGradient)
                            .clickableNoRipple { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "确定",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

/**
 * 会话重命名对话框
 *
 * @param initialTitle 初始标题
 * @param onConfirm 确认（返回新标题）
 * @param onDismiss 取消
 */
@Composable
fun RenameDialog(
    initialTitle: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "重命名对话",
                fontSize = 17.sp,
                color = Color(0xFF222222),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("请输入新名称", fontSize = 14.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(ConfirmGray)
                            .clickableNoRipple { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "取消",
                            fontSize = 15.sp,
                            color = ConfirmTextSecondary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(ConfirmGradient)
                            .clickableNoRipple { if (text.isNotBlank()) onConfirm(text.trim()) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "确定",
                            fontSize = 15.sp,
                            color = Color.White,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
            }
        },
        dismissButton = {}
    )
}
