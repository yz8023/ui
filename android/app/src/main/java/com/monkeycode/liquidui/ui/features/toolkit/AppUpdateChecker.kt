package com.monkeycode.liquidui.ui.features.toolkit

import com.monkeycode.liquidui.R

import androidx.compose.ui.res.stringResource

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * 应用内更新检查（手册 F18 / 条目 U01）：基于 GitHub Releases 的真实实现。
 *
 * 不引第三方依赖：`HttpURLConnection` 发请求，`org.json` 解析（Android 框架自带）。
 * 换成自己的后端只需改 [fetchLatestRelease] 里的 URL 与字段名，其余流程
 * （版本比较 → 三态 → 下载进度 → 安装）完全复用。
 *
 * 宿主接入需要三件事（示例 App 已配好，见 AndroidManifest 与 res/xml/file_paths.xml）：
 * 1. Manifest 声明 `INTERNET`；
 * 2. 想让用户直接装 APK，还要 `REQUEST_INSTALL_PACKAGES` + `FileProvider` +
 *    `res/xml/file_paths.xml` 里的 `<cache-path name="updates" path="updates/"/>`；
 * 3. 把 [DEFAULT_REPO] 改成你自己的 `owner/repo`，或调用时传参。
 */

/** 一次检查的结果。三态由 [state] 区分，不要用「有没有异常」来判断。 */
enum class UpdateState {
    /** 已经是最新版（含拿不到版本号但请求成功的情况）。 */
    UP_TO_DATE,

    /** 有新版本，可提示下载。 */
    UPDATE_AVAILABLE,

    /** 请求失败：无网、超时、仓库无 Release、API 限流（未带 token 时 60 次/小时/IP）。 */
    FAILED
}

data class ReleaseInfo(
    val tag: String,
    val name: String,
    val body: String,
    val publishedAt: String,
    val apkUrl: String?,
    val apkName: String?,
    val apkBytes: Long
)

data class UpdateCheckResult(
    val state: UpdateState,
    val currentVersion: String,
    val latest: ReleaseInfo?,
    val message: String,
    val elapsedMillis: Long
) {
    val hasUpdate: Boolean get() = state == UpdateState.UPDATE_AVAILABLE && latest != null
}

/** 下载进度回调：已写字节、总字节（-1 表示服务器没给 Content-Length）。 */
fun interface DownloadProgress {
    fun onProgress(writtenBytes: Long, totalBytes: Long)
}

object AppUpdateChecker {

    /** 示例 App 检查的是本模板仓库自己的 Release。宿主改成自己的 `owner/repo`。 */
    const val DEFAULT_REPO = "yz8023/ui"

    private const val TAG = "UpdateChecker"
    private const val CONNECT_TIMEOUT = 8_000
    private const val READ_TIMEOUT = 12_000

    /** APK 附件的匹配规则：优先 `.apk` 结尾，其次名字里含 release。 */
    private fun pickApkAsset(assets: List<Triple<String, String, Long>>): Triple<String, String, Long>? =
        assets.firstOrNull { it.first.endsWith(".apk", true) }
            ?: assets.firstOrNull { it.first.contains("release", true) }

    /**
     * 读当前安装版本名。用 PackageManager 而不是 BuildConfig——
     * 这样宿主不需要开 `buildConfig = true`，也不会因为构建类型不同而对不上。
     */
    fun currentVersionName(context: Context): String {
        // versionName 在较新 SDK 里标注为 @Nullable，所以先取出来再用 isNullOrBlank 判，
        // 不要写成 ?.takeIf { it.isNotBlank() } ?: 默认值 的长链（接收者可空性会变模糊）。
        val name = runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull()
        return if (name.isNullOrBlank()) "unknown" else name
    }

    /**
     * 语义化版本比较：`2.10.0 > 2.9.0`，忽略前导 `v`，缺位补 0，非数字段按 0 处理。
     * 返回 true 表示 [latest] 比 [current] 新。
     */
    fun isNewer(current: String, latest: String): Boolean {
        fun parts(s: String) = s.trim().removePrefix("v").removePrefix("V")
            .split(".").map { it.toIntOrNull() ?: 0 }
        val a = parts(current)
        val b = parts(latest)
        val n = maxOf(a.size, b.size)
        for (i in 0 until n) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (y > x) return true
            if (y < x) return false
        }
        return false
    }

    /**
     * 拉取最新 Release。**必须在 IO 线程调用**（`withContext(Dispatchers.IO)`）。
     * 私有仓库或想提高限流额度时，给 [token] 传值（会加 `Authorization: Bearer`）；
     * 不要把 token 硬编码进客户端，那等于公开。
     */
    suspend fun check(
        context: Context,
        repo: String = DEFAULT_REPO,
        token: String? = null
    ): UpdateCheckResult {
        val started = System.currentTimeMillis()
        val current = currentVersionName(context)
        return runCatching {
            val url = URL("https://api.github.com/repos/$repo/releases/latest")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "liquidui-template-update-check")
                token?.let { setRequestProperty("Authorization", "Bearer $it") }
            }
            AppLog.i(TAG, "GET $url")
            val code = conn.responseCode
            if (code !in 200..299) {
                conn.disconnect()
                AppLog.w(TAG, "HTTP $code")
                return UpdateCheckResult(
                    state = UpdateState.FAILED,
                    currentVersion = current,
                    latest = null,
                    message = when (code) {
                        403 -> "HTTP 403：多半是 API 限流（未带 token 时 60 次/小时/IP），稍后再试"
                        404 -> "HTTP 404：仓库不存在或还没有发布任何 Release"
                        else -> "HTTP $code"
                    },
                    elapsedMillis = System.currentTimeMillis() - started
                )
            }
            val raw = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val json = org.json.JSONObject(raw)
            val tag = json.optString("tag_name", "")
            val assets = mutableListOf<Triple<String, String, Long>>()
            val arr = json.optJSONArray("assets") ?: org.json.JSONArray()
            for (i in 0 until arr.length()) {
                val a = arr.optJSONObject(i) ?: continue
                assets += Triple(
                    a.optString("name"),
                    a.optString("browser_download_url"),
                    a.optLong("size", -1L)
                )
            }
            val apk = pickApkAsset(assets)
            val info = ReleaseInfo(
                tag = tag,
                name = json.optString("name", tag),
                body = json.optString("body", ""),
                publishedAt = json.optString("published_at", ""),
                apkUrl = apk?.second?.takeIf { it.isNotBlank() },
                apkName = apk?.first,
                apkBytes = apk?.third ?: -1L
            )
            val newer = tag.isNotBlank() && isNewer(current, tag)
            AppLog.i(TAG, "latest=$tag current=$current newer=$newer assets=${assets.size}")
            UpdateCheckResult(
                state = if (newer) UpdateState.UPDATE_AVAILABLE else UpdateState.UP_TO_DATE,
                currentVersion = current,
                latest = info,
                message = when {
                    tag.isBlank() -> "该仓库还没有发布 Release"
                    newer -> context.getString(R.string.update_text_1, tag, current)
                    else -> context.getString(R.string.update_text_2, tag)
                },
                elapsedMillis = System.currentTimeMillis() - started
            )
        }.getOrElse { t ->
            AppLog.e(TAG, "检查失败", t)
            UpdateCheckResult(
                state = UpdateState.FAILED,
                currentVersion = current,
                latest = null,
                message = when (t) {
                    is java.net.UnknownHostException -> "无法解析 api.github.com：检查网络或代理"
                    is java.net.SocketTimeoutException -> context.getString(R.string.update_text_3, CONNECT_TIMEOUT, READ_TIMEOUT)
                    is javax.net.ssl.SSLException -> context.getString(R.string.update_text_4, t.message)
                    else -> "${t.javaClass.simpleName}: ${t.message}"
                },
                elapsedMillis = System.currentTimeMillis() - started
            )
        }
    }

    /** 下载目录：应用私有缓存，卸载即清，不需要存储权限。 */
    fun updateDir(context: Context): File = File(context.cacheDir, "updates").apply { mkdirs() }

    /**
     * 下载 APK 到私有缓存目录，边下边报进度。**必须在 IO 线程调用**。
     * 返回落盘文件；失败抛异常，由调用方转成用户可读的文案。
     * 断点续传没做（模板级实现从简），大文件场景自己加 `Range` 头。
     */
    suspend fun download(
        context: Context,
        url: String,
        fileName: String = "update.apk",
        onProgress: DownloadProgress
    ): File {
        val dest = File(updateDir(context), fileName)
        val tmp = File(updateDir(context), "$fileName.part")
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT
            readTimeout = 60_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "liquidui-template-updater")
        }
        val total = conn.contentLengthLong
        AppLog.i(TAG, context.getString(R.string.update_text_5, total, tmp.absolutePath))
        var written = 0L
        conn.inputStream.use { input ->
            tmp.outputStream().use { output ->
                val buf = ByteArray(64 * 1024)
                while (true) {
                    val n = input.read(buf)
                    if (n <= 0) break
                    output.write(buf, 0, n)
                    written += n
                    onProgress.onProgress(written, total)
                }
            }
        }
        conn.disconnect()
        if (total > 0 && written != total) {
            tmp.delete()
            throw java.io.IOException(context.getString(R.string.update_text_6, written, total))
        }
        if (dest.exists()) dest.delete()
        if (!tmp.renameTo(dest)) throw java.io.IOException(context.getString(R.string.update_text_7, tmp.name, dest.name))
        AppLog.i(TAG, context.getString(R.string.update_text_8, dest.length()))
        return dest
    }

    /**
     * 调起系统安装器。Android 8+ 需要「安装未知应用」授权，未授权时返回 false，
     * 调用方应改用 [openUnknownSourcesSettings] 引导用户去开——**不要静默失败**。
     */
    fun installApk(context: Context, apk: File, authority: String = "${context.packageName}.fileprovider"): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            AppLog.w(TAG, "未授予「安装未知应用」，转设置页")
            return false
        }
        val uri: Uri = FileProvider.getUriForFile(context, authority, apk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        AppLog.i(TAG, context.getString(R.string.update_text_9, apk.name, apk.length()))
        return runCatching { context.startActivity(intent); true }
            .getOrElse { AppLog.e(TAG, "调起安装器失败", it); false }
    }

    /** 打开「安装未知应用」授权页（Android 8+）。 */
    fun openUnknownSourcesSettings(context: Context) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return
        val intent = Intent(
            android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
            .onFailure { AppLog.e(TAG, "打不开未知来源设置页", it) }
    }

    /** 打不开安装器时的兜底：用浏览器下载。 */
    fun openInBrowser(context: Context, url: String) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }.onFailure { AppLog.e(TAG, "打不开浏览器", it) }
    }

    /** 字节数转人类可读，进度文案用。 */
    fun humanSize(bytes: Long): String = when {
        bytes < 0 -> "未知大小"
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${"%.0f".format(bytes / 1024.0)} KB"
        else -> "${"%.2f".format(bytes / 1024.0 / 1024.0)} MB"
    }
}
