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
 * UI尺寸参考：
 * - 输入框最小高度: 48.dp
 * - 输入框最大高度: 120.dp
 * - 输入框圆角: 24.dp
 * - 图标大小: 32.dp
 * - 图标按钮大小: 52.dp
 * - 快捷操作按钮高度: 32.dp
 * - 工具按钮大小: 48.dp
 */
package com.example.androidfronted.ui.smartcustomerservice.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.androidfronted.R

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
 * 
 * 包含快捷操作栏、输入框、功能按钮和工具栏
 * 
 * @param inputText 当前输入文本
 * @param isLoading 是否正在加载
 * @param inputMode 当前输入模式
 * @param showTools 是否显示工具栏
 * @param onInputChange 输入文本变化回调
 * @param onSendClick 发送按钮点击回调
 * @param onCancelClick 取消按钮点击回调
 * @param onInputModeChange 输入模式变化回调
 * @param onCameraClick 相机按钮点击回调
 * @param onToggleTools 切换工具栏显示状态回调
 * @param onPhotoClick 拍照按钮点击回调
 * @param onGalleryClick 相册按钮点击回调
 * @param onFileClick 文件夹按钮点击回调
 * @param modifier 修饰符
 */
@Composable
fun InputArea(
    inputText: String,
    isLoading: Boolean,
    inputMode: InputMode,
    showTools: Boolean,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onCancelClick: () -> Unit,
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
        modifier = modifier.fillMaxWidth()
    ) {
        // 快捷操作栏 - 横向滚动的常用功能
        QuickActionsBar(
            modifier = Modifier.fillMaxWidth()
        )
        
        // 分隔线 - 位于快捷操作栏和输入栏之间
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        // 输入栏主体
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
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
                            // 设置按钮颜色
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.bg_top_bar), // 背景颜色
                                contentColor = colorResource(id = R.color.white)               // 内容(文字)颜色
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
        
        // 工具栏 - 拍照、相册、文件夹
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
 * 快捷操作栏组件
 * 
 * 横向滚动的常用功能按钮列表
 * 
 * @param modifier 修饰符
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
                label = { Text(title) },
                modifier = Modifier.height(32.dp)
            )
        }
    }
}

/**
 * 工具栏组件
 * 
 * 显示拍照、相册、文件夹三个工具按钮
 * 
 * @param onPhotoClick 拍照按钮点击回调
 * @param onGalleryClick 相册按钮点击回调
 * @param onFileClick 文件夹按钮点击回调
 * @param modifier 修饰符
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
        color = MaterialTheme.colorScheme.surfaceVariant
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
 * 
 * 单个工具按钮，包含图标和文字
 * 
 * @param iconRes 图标资源ID
 * @param text 按钮文字
 * @param onClick 点击回调
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
