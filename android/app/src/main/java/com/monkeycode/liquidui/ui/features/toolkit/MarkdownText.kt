package com.monkeycode.liquidui.ui.features.toolkit

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Markdown 渲染（手册 F20 / 条目 U03）：自写解析器 + Compose 渲染，零第三方依赖。
 *
 * 支持的语法（够渲染 README、更新日志、说明页）：
 * `# ~ ######` 标题、`**粗**`、`*斜*`/`_斜_`、`` `行内码` ``、`[文字](链接)`、
 * 围栏代码块 ```、无序列表 `- * +`、有序列表 `1.`、引用 `>`、分隔线 `--- *** ___`。
 * **不支持**：表格、图片、嵌套列表、HTML 内联、任务列表——需要时要么补解析器，
 * 要么引 `org.commonmark:commonmark`（Kit 那类项目就是这么做的，多约 200KB 依赖）。
 *
 * 链接点击走 `LocalUriHandler`，不需要自己写 Intent；代码块用 `SelectionContainer` 可长按选中复制。
 */

sealed interface MdInline

data class MdText(val text: String) : MdInline
data class MdBold(val text: String) : MdInline
data class MdItalic(val text: String) : MdInline
data class MdCode(val text: String) : MdInline
data class MdLink(val text: String, val url: String) : MdInline

sealed interface MdBlock

data class MdHeading(val level: Int, val inlines: List<MdInline>) : MdBlock
data class MdParagraph(val inlines: List<MdInline>) : MdBlock
data class MdBulletList(val items: List<List<MdInline>>) : MdBlock
data class MdOrderedList(val items: List<List<MdInline>>, val start: Int) : MdBlock
data class MdCodeBlock(val language: String, val code: String) : MdBlock
data class MdQuote(val inlines: List<MdInline>) : MdBlock
data object MdRule : MdBlock

object MarkdownParser {

    /** 把整篇 Markdown 解析成块列表。空行分隔段落，围栏代码块内部原样保留。 */
    fun parse(source: String): List<MdBlock> {
        val lines = source.replace("\r\n", "\n").split("\n")
        val blocks = mutableListOf<MdBlock>()
        var i = 0
        val paragraph = mutableListOf<String>()

        fun flushParagraph() {
            if (paragraph.isEmpty()) return
            blocks += MdParagraph(parseInlines(paragraph.joinToString(" ")))
            paragraph.clear()
        }

        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            // 围栏代码块：``` 或 ~~~，可带语言名
            if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                flushParagraph()
                val fence = trimmed.take(3)
                val lang = trimmed.removePrefix(fence).trim()
                val body = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith(fence)) {
                    body += lines[i]
                    i++
                }
                i++ // 跳过结束围栏
                blocks += MdCodeBlock(lang, body.joinToString("\n"))
                continue
            }

            when {
                trimmed.isEmpty() -> flushParagraph()

                trimmed.matches(Regex("^(-{3,}|\\*{3,}|_{3,})$")) -> {
                    flushParagraph()
                    blocks += MdRule
                }

                trimmed.startsWith("#") -> {
                    flushParagraph()
                    val level = trimmed.takeWhile { it == '#' }.length.coerceIn(1, 6)
                    blocks += MdHeading(level, parseInlines(trimmed.drop(level).trim()))
                }

                trimmed.startsWith(">") -> {
                    flushParagraph()
                    val quote = mutableListOf<String>()
                    while (i < lines.size && lines[i].trim().startsWith(">")) {
                        quote += lines[i].trim().removePrefix(">").trim()
                        i++
                    }
                    i-- // 交回给外层 while 的 i++
                    blocks += MdQuote(parseInlines(quote.joinToString(" ")))
                }

                trimmed.matches(Regex("^([-*+])\\s+.*")) -> {
                    flushParagraph()
                    val items = mutableListOf<List<MdInline>>()
                    while (i < lines.size && lines[i].trim().matches(Regex("^([-*+])\\s+.*"))) {
                        items += parseInlines(lines[i].trim().drop(1).trim())
                        i++
                    }
                    i--
                    blocks += MdBulletList(items)
                }

                trimmed.matches(Regex("^\\d+[.)]\\s+.*")) -> {
                    flushParagraph()
                    val items = mutableListOf<List<MdInline>>()
                    var start = 1
                    var first = true
                    while (i < lines.size && lines[i].trim().matches(Regex("^\\d+[.)]\\s+.*"))) {
                        val t = lines[i].trim()
                        val marker = t.takeWhile { it.isDigit() }
                        if (first) {
                            start = marker.toIntOrNull() ?: 1
                            first = false
                        }
                        items += parseInlines(t.dropWhile { it.isDigit() || it == '.' || it == ')' }.trim())
                        i++
                    }
                    i--
                    blocks += MdOrderedList(items, start)
                }

                else -> paragraph += trimmed
            }
            i++
        }
        flushParagraph()
        return blocks
    }

    /**
     * 行内解析：按 `` ` `` → `[]()` → `**` → `*`/`_` 的优先级扫描。
     * 刻意不做嵌套（`**粗里有`码`**` 会被当成粗体包纯文本），模板级实现够用且好维护。
     */
    fun parseInlines(text: String): List<MdInline> {
        val out = mutableListOf<MdInline>()
        val plain = StringBuilder()
        var i = 0

        fun flushPlain() {
            if (plain.isNotEmpty()) {
                out += MdText(plain.toString())
                plain.setLength(0)
            }
        }

        while (i < text.length) {
            val c = text[i]
            // 行内代码
            if (c == '`') {
                val end = text.indexOf('`', i + 1)
                if (end > i) {
                    flushPlain()
                    out += MdCode(text.substring(i + 1, end))
                    i = end + 1
                    continue
                }
            }
            // 链接 [text](url)
            if (c == '[') {
                val close = text.indexOf(']', i + 1)
                if (close > i && close + 1 < text.length && text[close + 1] == '(') {
                    val urlEnd = text.indexOf(')', close + 2)
                    if (urlEnd > close) {
                        flushPlain()
                        out += MdLink(text.substring(i + 1, close), text.substring(close + 2, urlEnd))
                        i = urlEnd + 1
                        continue
                    }
                }
            }
            // 粗体 **text**
            if (c == '*' && i + 1 < text.length && text[i + 1] == '*') {
                val end = text.indexOf("**", i + 2)
                if (end > i) {
                    flushPlain()
                    out += MdBold(text.substring(i + 2, end))
                    i = end + 2
                    continue
                }
            }
            // 斜体 *text* 或 _text_
            if (c == '*' || c == '_') {
                val end = text.indexOf(c, i + 1)
                if (end > i + 1 && (c != '_' || text.getOrNull(end + 1)?.isLetterOrDigit() != true)) {
                    flushPlain()
                    out += MdItalic(text.substring(i + 1, end))
                    i = end + 1
                    continue
                }
            }
            plain.append(c)
            i++
        }
        flushPlain()
        return out
    }
}

/** 把行内节点渲染成带样式的 AnnotatedString（链接可点、代码等宽）。 */
@Composable
fun rememberInlineText(inlines: List<MdInline>): AnnotatedString {
    val linkColor = MaterialTheme.colorScheme.primary
    val codeColor = MaterialTheme.colorScheme.tertiary
    return remember(inlines, linkColor, codeColor) {
        buildAnnotatedString {
            inlines.forEach { node ->
                when (node) {
                    is MdText -> append(node.text)
                    is MdBold -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(node.text) }
                    is MdItalic -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(node.text) }
                    is MdCode -> withStyle(
                        SpanStyle(fontFamily = FontFamily.Monospace, color = codeColor, fontSize = 13.sp)
                    ) { append(node.text) }
                    is MdLink -> withLink(LinkAnnotation.Url(node.url)) {
                        withStyle(SpanStyle(color = linkColor, fontWeight = FontWeight.Medium)) { append(node.text) }
                    }
                }
            }
        }
    }
}

/** 渲染整篇 Markdown。[source] 变化会自动重新解析（remember  keyed）。 */
@Composable
fun MarkdownText(source: String, modifier: Modifier = Modifier) {
    val blocks = remember(source) { MarkdownParser.parse(source) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MdHeading -> {
                    val style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineSmall
                        2 -> MaterialTheme.typography.titleLarge
                        3 -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.titleSmall
                    }
                    Text(
                        text = rememberInlineText(block.inlines),
                        style = style,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = if (block.level <= 2) 6.dp else 0.dp)
                    )
                }

                is MdParagraph -> Text(
                    text = rememberInlineText(block.inlines),
                    style = MaterialTheme.typography.bodyMedium
                )

                is MdQuote -> Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .width(3.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                    )
                    Text(
                        text = rememberInlineText(block.inlines),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                is MdBulletList -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    block.items.forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("•", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Text(rememberInlineText(item), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                is MdOrderedList -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    block.items.forEachIndexed { index, item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "${block.start + index}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(rememberInlineText(item), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                is MdCodeBlock -> Column(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    if (block.language.isNotBlank()) {
                        Text(
                            block.language,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 12.dp, top = 8.dp)
                        )
                    }
                    SelectionContainer {
                        Text(
                            text = block.code,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(12.dp)
                        )
                    }
                }

                MdRule -> HorizontalDivider(Modifier.padding(vertical = 2.dp))
            }
        }
    }
}
