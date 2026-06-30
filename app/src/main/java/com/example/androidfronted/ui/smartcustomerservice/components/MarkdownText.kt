package com.example.androidfronted.ui.smartcustomerservice.components

import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import com.example.androidfronted.utils.MarkwonProvider
import kotlinx.coroutines.delay

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    textSize: Float = 16f,
    textAlign: TextAlign = TextAlign.Start,
    isStreaming: Boolean = false
) {
    var debouncedMarkdown by remember { mutableStateOf(markdown) }

    LaunchedEffect(markdown) {
        if (isStreaming) {
            delay(20)
        }
        debouncedMarkdown = markdown
    }

    val processedMarkdown = debouncedMarkdown
        .replace("\\r\\n", "\n")
        .replace("\\n", "\n")
        .replace("\\r", "\n")
        .replace("\\t", "\t")
        .replace("\\\"", "\"")
        .let { ensureBlankLineBeforeBlockElements(it) }
        .let { convertSingleNewlineToHardBreak(it) }
        .let { if (isStreaming) it.patchMarkdownForStreaming() else it }

    val displayText = if (isStreaming) {
        processedMarkdown + "▌"
    } else {
        processedMarkdown
    }

    AndroidView(
        factory = { context ->
            TextView(context).apply {
                setTextColor(textColor.toArgb())
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, textSize)
                this.textAlignment = when (textAlign) {
                    TextAlign.Center -> TextView.TEXT_ALIGNMENT_CENTER
                    TextAlign.End -> TextView.TEXT_ALIGNMENT_TEXT_END
                    else -> TextView.TEXT_ALIGNMENT_TEXT_START
                }
                movementMethod = android.text.method.LinkMovementMethod.getInstance()
            }
        },
        update = { textView ->
            try {
                MarkwonProvider.markwon.setMarkdown(textView, displayText)
            } catch (e: Exception) {
                textView.text = displayText
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

private fun ensureBlankLineBeforeBlockElements(text: String): String {
    val lines = text.lines()
    val result = mutableListOf<String>()

    fun isTableRow(line: String): Boolean {
        val t = line.trim()
        return t.startsWith("|") && t.lastIndexOf("|") > 0
    }

    for (i in lines.indices) {
        val line = lines[i]
        val trimmed = line.trim()

        val isTableStart = isTableRow(line) && (i == 0 || !isTableRow(lines[i - 1]))
        val isHeading = trimmed.matches(Regex("^#{1,6}\\s+\\S"))
        val isThematicBreak = trimmed.matches(Regex("^(---|\\*\\*\\*|___)\\s*$"))
        val isBlockquote = trimmed.startsWith(">") && (i == 0 || !lines[i - 1].trim().startsWith(">"))

        if ((isTableStart || isHeading || isThematicBreak || isBlockquote) && i > 0) {
            val prevLine = result.lastOrNull()?.trim() ?: ""
            if (prevLine.isNotEmpty()) {
                result.add("")
            }
        }

        result.add(line)
    }

    return result.joinToString("\n")
}

private fun convertSingleNewlineToHardBreak(text: String): String {
    val lines = text.lines()
    val result = mutableListOf<String>()
    var inCodeBlock = false

    for (i in lines.indices) {
        val line = lines[i]
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            inCodeBlock = !inCodeBlock
            result.add(line)
            continue
        }

        if (inCodeBlock) {
            result.add(line)
            continue
        }

        val isTableRow = trimmed.startsWith("|") && trimmed.lastIndexOf("|") > 0
        val nextLine = lines.getOrNull(i + 1)?.trim() ?: ""

        if (!isTableRow && trimmed.isNotEmpty() && nextLine.isNotEmpty()) {
            result.add(line.trimEnd() + "  ")
        } else {
            result.add(line)
        }
    }

    return result.joinToString("\n")
}

private fun String.patchMarkdownForStreaming(): String {
    var text = this

    val boldCount = text.split("**").size - 1
    if (boldCount % 2 != 0) {
        text += "**"
    }

    val italicMarkers = countItalicMarkers(text)
    if (italicMarkers % 2 != 0) {
        text += "*"
    }

    val codeBlockCount = text.split("```").size - 1
    if (codeBlockCount % 2 != 0) {
        text += "\n```"
    }

    val allBackticks = text.count { it == '`' }
    val codeBlockBackticks = codeBlockCount * 3
    val inlineCodeBackticks = allBackticks - codeBlockBackticks
    if (inlineCodeBackticks % 2 != 0) {
        text += "`"
    }

    val openBrackets = text.count { it == '[' }
    val closeBrackets = text.count { it == ']' }
    if (openBrackets > closeBrackets) {
        repeat(openBrackets - closeBrackets) { text += "]" }
    }

    val openParens = text.count { it == '(' }
    val closeParens = text.count { it == ')' }
    if (openParens > closeParens) {
        repeat(openParens - closeParens) { text += ")" }
    }

    val lines = text.lines()
    val patchedLines = mutableListOf<String>()
    for (i in lines.indices) {
        val line = lines[i]
        val trimmed = line.trim()

        if (trimmed.matches(Regex("^#{1,6}$")) && i < lines.size - 1) {
            continue
        }

        if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
            patchedLines.add(line)
            continue
        }

        if (trimmed.matches(Regex("^#{1,6}\\s*$"))) {
            continue
        }

        patchedLines.add(line)
    }
    text = patchedLines.joinToString("\n")

    return text
}

private fun countItalicMarkers(text: String): Int {
    val withoutBold = text.replace("**", "")
    return withoutBold.count { it == '*' }
}
