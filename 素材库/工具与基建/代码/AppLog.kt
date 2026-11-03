package com.monkeycode.liquidui.ui.features.toolkit

import com.monkeycode.liquidui.R

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.res.stringResource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 应用内运行日志（手册 F19 / 条目 U02）：真实可用的环形缓冲日志。
 *
 * 与 `android.util.Log` 的区别是**能被应用自己读到**：logcat 从 Android 4.1 起
 * 不再允许普通应用读其他进程（甚至自己）的完整日志，所以「分享日志」这类功能
 * 必须自己存一份。这里存内存不存文件——进程被杀即清空，也因此不需要存储权限、
 * 不会在用户设备上留痕。需要跨进程留存时，把 [persist] 换成写
 * `context.getExternalFilesDir(null)` 下的文件即可（那条路要自己处理轮转与体积上限）。
 *
 * 用法：在真实调用点打日志（网络请求起止、权限结果、异常捕获），
 * 然后用 [dump] 导出文本、[buildShareIntent] 分享。
 */
enum class AppLogLevel(val label: String) {
    DEBUG("D"), INFO("I"), WARN("W"), ERROR("E")
}

/** 一条日志。[seq] 单调递增，分享时用于判断有没有丢条目。 */
data class AppLogEntry(
    val seq: Long,
    val timeMillis: Long,
    val level: AppLogLevel,
    val tag: String,
    val message: String
) {
    fun format(timeFormat: SimpleDateFormat): String =
        "${timeFormat.format(Date(timeMillis))} $seq ${level.label}/$tag: $message"
}

object AppLog {

    /** 缓冲上限：超过就丢最旧的。400 条足够一次问题复现，又不会吃内存。 */
    const val MAX_ENTRIES = 400

    /** Compose 可直接观察的列表（SnapshotStateList），界面随写入自动刷新。 */
    val entries = mutableStateListOf<AppLogEntry>()

    private var counter = 0L

    /**
     * 需要落盘时把这个 lambda 换成写文件的实现（默认不落盘）。
     * 参数是刚写入的那一条。
     */
    var persist: ((AppLogEntry) -> Unit)? = null

    @Synchronized
    fun log(level: AppLogLevel, tag: String, message: String, throwable: Throwable? = null) {
        val full = if (throwable == null) message else "$message\n${throwable.javaClass.simpleName}: ${throwable.message}"
        val entry = AppLogEntry(
            seq = ++counter,
            timeMillis = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = full
        )
        entries.add(entry)
        while (entries.size > MAX_ENTRIES) entries.removeAt(0)
        // 同时写 logcat，开发时两边都能看到。
        when (level) {
            AppLogLevel.DEBUG -> android.util.Log.d(tag, full)
            AppLogLevel.INFO -> android.util.Log.i(tag, full)
            AppLogLevel.WARN -> android.util.Log.w(tag, full)
            AppLogLevel.ERROR -> android.util.Log.e(tag, full, throwable)
        }
        persist?.invoke(entry)
    }

    fun d(tag: String, message: String) = log(AppLogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(AppLogLevel.INFO, tag, message)
    fun w(tag: String, message: String) = log(AppLogLevel.WARN, tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) =
        log(AppLogLevel.ERROR, tag, message, throwable)

    @Synchronized
    fun clear() {
        entries.clear()
    }

    /** 各级别条数，用于「共 n 条 · 错误 m 条」这类摘要。 */
    fun counts(): Map<AppLogLevel, Int> {
        val map = AppLogLevel.entries.associateWith { 0 }.toMutableMap()
        entries.forEach { map[it.level] = (map[it.level] ?: 0) + 1 }
        return map
    }

    /**
     * 导出纯文本。[level] 为 null 时导出全部；[keyword] 非空时按 tag 或正文包含关系过滤。
     * 首行带条数与时间范围，方便对方一眼看出日志是否完整。
     */
    fun dump(level: AppLogLevel? = null, keyword: String = ""): String {
        val format = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)
        val picked = entries.filter { e ->
            (level == null || e.level == level) &&
                (keyword.isBlank() || e.tag.contains(keyword, true) || e.message.contains(keyword, true))
        }
        val header = buildString {
            append("应用日志导出 · 共 ${picked.size} 条")
            if (picked.isNotEmpty()) {
                append("（缓冲 ${entries.size}/$MAX_ENTRIES）")
                append(" · ${format.format(Date(picked.first().timeMillis))} → ${format.format(Date(picked.last().timeMillis))}")
            }
            level?.let { append(" · 仅 ${it.label} 级") }
            if (keyword.isNotBlank()) append(" · 关键词「$keyword」")
        }
        return picked.fold(header) { acc, e -> acc + "\n" + e.format(format) }
    }

    /**
     * 构造分享 Intent（`ACTION_SEND` + `text/plain`）。调用方用
     * `context.startActivity(Intent.createChooser(intent, "分享日志"))`，
     * 并在外面包 `runCatching`——用户可能没装任何能接收的应用。
     */
    fun buildShareIntent(context: Context, level: AppLogLevel? = null, keyword: String = ""): Intent {
        val text = dump(level, keyword)
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_log_text_1, entries.size))
            putExtra(Intent.EXTRA_TEXT, text)
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
