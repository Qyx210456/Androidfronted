/**
 * 输入区域组件
 *
 * 该文件包含智能客服聊天界面的底部输入区域组件，包括：
 * - InputArea: 主输入区域组件
 * - QuickActionsBar: 快捷操作栏（横向滚动的常用功能）
 * - ToolsBar: 工具栏（拍照、相册、文件夹）
 * - ToolButton: 工具按钮组件
 *
 * 功能说明：
 * 1. 支持键盘输入和语音输入两种模式切换
 * 2. 输入框有内容时显示发送按钮，无内容时显示相机按钮
 * 3. 点击加号按钮可展开/收起工具栏
 * 4. 点击工具栏按钮时自动收起键盘
 *
 * 样式：
 * - 推荐栏与输入栏背景白色
 * - 推荐功能组件四周的边框颜色减淡，突出中间文本内容
 */
package com.example.androidfronted.ui.smartcustomerservice.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidfronted.R

private val InputBarWhite = Color.White
private val InputDivider = Color(0xFFEEEEEE)
private val ChipLabelColor = Color(0xFF333333)
/** chip浅灰边框 */
private val ChipBorderColor = Color(0xFFDDDDDD)

/**
 * 输入模式枚举
 * - KEYBOARD: 键盘输入模式
 * - VOICE: 语音输入模式
 */
enum class InputMode {
    KEYBOARD,
    VOICE
}

/**
 * 输入区域主组件
 */
@Composable
fun InputArea(
    inputText: String,
    isLoading: Boolean,
    inputMode: InputMode,
    showTools: Boolean,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onInputModeChange: (InputMode) -> Unit,
    onCameraClick: () -> Unit,
    onToggleTools: () -> Unit,
    onPhotoClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onFileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(InputBarWhite)
    ) {
        // 快捷操作栏 - 横向滚动的常用功能（白色背景）
        QuickActionsBar(
            modifier = Modifier
                .fillMaxWidth()
                .background(InputBarWhite)
        )

        // 输入栏主体（白色背景）
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = InputBarWhite
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 语音/键盘切换按钮
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        onInputModeChange(if (inputMode == InputMode.KEYBOARD) InputMode.VOICE else InputMode.KEYBOARD)
                    },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (inputMode == InputMode.KEYBOARD) R.drawable.ic_smart_customer_service_voice
                            else R.drawable.ic_smart_customer_service_keyboard
                        ),
                        contentDescription = if (inputMode == InputMode.KEYBOARD) "语音输入" else "键盘输入",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // 键盘输入模式
                if (inputMode == InputMode.KEYBOARD) {
                    // 输入框
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp, max = 120.dp),
                        placeholder = { Text("发消息") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        enabled = !isLoading
                    )

                    // 有输入内容时显示：加号 + 发送按钮
                    if (inputText.isNotBlank()) {
                        // 加号按钮（在原来相机的位置）
                        androidx.compose.foundation.Image(
                            painter = painterResource(
                                id = if (showTools) R.drawable.ic_smart_customer_service_close_add
                                else R.drawable.ic_smart_customer_service_add
                            ),
                            contentDescription = if (showTools) "关闭" else "更多功能",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    keyboardController?.hide()
                                    onToggleTools()
                                }
                        )
                        // 发送按钮（使用图标）
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.ic_smart_customer_service_sent_message),
                            contentDescription = "发送",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    keyboardController?.hide()
                                    onSendClick()
                                }
                        )
                    } else {
                        // 无输入内容时显示：相机 + 加号按钮
                        // 相机按钮
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.ic_smart_customer_service_camera),
                            contentDescription = "相机",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    keyboardController?.hide()
                                    onCameraClick()
                                }
                        )
                        // 加号按钮
                        androidx.compose.foundation.Image(
                            painter = painterResource(
                                id = if (showTools) R.drawable.ic_smart_customer_service_close_add
                                else R.drawable.ic_smart_customer_service_add
                            ),
                            contentDescription = if (showTools) "关闭" else "更多功能",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    keyboardController?.hide()
                                    onToggleTools()
                                }
                        )
                    }
                } else {
                    // 语音输入模式
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(24.dp),
                            enabled = !isLoading,
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.bg_top_bar),
                                contentColor = colorResource(id = R.color.white)
                            )
                        ) {
                            Text("按住说话")
                        }
                    }

                    // 相机按钮
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.ic_smart_customer_service_camera),
                        contentDescription = "相机",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                keyboardController?.hide()
                                onCameraClick()
                            }
                    )

                    // 加号按钮
                    androidx.compose.foundation.Image(
                        painter = painterResource(
                            id = if (showTools) R.drawable.ic_smart_customer_service_close_add
                            else R.drawable.ic_smart_customer_service_add
                        ),
                        contentDescription = if (showTools) "关闭" else "更多功能",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                keyboardController?.hide()
                                onToggleTools()
                            }
                    )
                }
            }
        }

        // 工具栏 - 拍照、相册、文件夹（白色背景）
        AnimatedVisibility(
            visible = showTools,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            ToolsBar(
                onPhotoClick = {
                    keyboardController?.hide()
                    onPhotoClick()
                },
                onGalleryClick = {
                    keyboardController?.hide()
                    onGalleryClick()
                },
                onFileClick = {
                    keyboardController?.hide()
                    onFileClick()
                }
            )
        }
    }
}

/**
 * 快捷操作栏组件：白色背景 + 减淡边框，突出中间文本
 */
@Composable
private fun QuickActionsBar(
    modifier: Modifier = Modifier
) {
    val quickActions = listOf(
        "贷款咨询" to "贷款相关问题",
        "还款计算" to "计算还款金额",
        "申请进度" to "查询申请进度",
        "政策解读" to "贷款政策咨询"
    )

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        quickActions.forEach { (title, _) ->
            SuggestionChip(
                onClick = { },
                label = {
                    Text(
                        title,
                        fontSize = 13.sp,
                        color = ChipLabelColor
                    )
                },
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = InputBarWhite,
                    labelColor = ChipLabelColor
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = true,
                    borderColor = ChipBorderColor
                )
            )
        }
    }
}

/**
 * 工具栏组件：白色背景
 */
@Composable
private fun ToolsBar(
    onPhotoClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onFileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = InputBarWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToolButton(
                iconRes = R.drawable.ic_smart_customer_service_camera,
                text = "拍照",
                onClick = onPhotoClick
            )
            ToolButton(
                iconRes = R.drawable.ic_smart_customer_service_gallery,
                text = "相册",
                onClick = onGalleryClick
            )
            ToolButton(
                iconRes = R.drawable.ic_smart_customer_service_file,
                text = "文件夹",
                onClick = onFileClick
            )
        }
    }
}

/**
 * 工具按钮组件
 */
@Composable
private fun ToolButton(
    iconRes: Int,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = text,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
