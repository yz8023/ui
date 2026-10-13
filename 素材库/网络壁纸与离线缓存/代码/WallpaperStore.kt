package com.monkeycode.liquidui.ui.features

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** A wallpaper copied into app-private storage, independent from its original URL/URI. */
data class CachedWallpaper(val file: File, val width: Int, val height: Int)

/**
 * Durable wallpaper cache. Both document images and network images are decoded,
 * size-limited, downsampled and re-encoded into app-private storage. A dead URL,
 * revoked document permission or deleted source file therefore cannot remove the wallpaper.
 */
object WallpaperStore {
    private const val MAX_DOWNLOAD_BYTES = 20 * 1024 * 1024
    private const val MAX_DIMENSION = 4096
    private const val MAX_SOURCE_PIXELS = 80_000_000L
    private const val FILE_NAME = "active_wallpaper.webp"

    fun cachedFile(context: Context): File = File(context.filesDir, "wallpapers/$FILE_NAME")

    suspend fun cacheNetwork(context: Context, rawUrl: String): Result<CachedWallpaper> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = URL(rawUrl.trim())
                require(url.protocol.equals("https", true)) {
                    "仅支持 HTTPS 图片链接"
                }
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 12_000
                    readTimeout = 20_000
                    instanceFollowRedirects = true
                    requestMethod = "GET"
                    setRequestProperty("Accept", "image/*")
                    setRequestProperty("User-Agent", "LiquidUI-Wallpaper/1.0")
                }
                try {
                    connection.connect()
                    require(connection.responseCode in 200..299) { "下载失败：HTTP ${connection.responseCode}" }
                    val length = connection.contentLengthLong
                    require(length <= 0 || length <= MAX_DOWNLOAD_BYTES) { "图片超过 20MB 限制" }
                    val bytes = connection.inputStream.use(::readLimited)
                    decodeAndPersist(context, bytes)
                } finally {
                    connection.disconnect()
                }
            }
        }

    suspend fun cacheDocument(context: Context, uri: Uri): Result<CachedWallpaper> =
        withContext(Dispatchers.IO) {
            runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use(::readLimited)
                    ?: error("无法读取所选图片")
                decodeAndPersist(context, bytes)
            }
        }

    fun clear(context: Context) {
        cachedFile(context).delete()
    }

    private fun readLimited(input: java.io.InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(16 * 1024)
        var total = 0
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            require(total <= MAX_DOWNLOAD_BYTES) { "图片超过 20MB 限制" }
            output.write(buffer, 0, read)
        }
        require(total > 0) { "图片内容为空" }
        return output.toByteArray()
    }

    private fun decodeAndPersist(context: Context, bytes: ByteArray): CachedWallpaper {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "链接内容不是可识别的图片" }
        require(bounds.outWidth.toLong() * bounds.outHeight <= MAX_SOURCE_PIXELS) { "图片分辨率过大" }

        var sample = 1
        while (bounds.outWidth / sample > MAX_DIMENSION || bounds.outHeight / sample > MAX_DIMENSION) {
            sample *= 2
        }
        val bitmap = BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            },
        ) ?: error("图片解码失败")

        val file = cachedFile(context)
        file.parentFile?.mkdirs()
        val temporary = File(file.parentFile, "$FILE_NAME.tmp")
        try {
            FileOutputStream(temporary).use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.WEBP, 92, output)) { "壁纸缓存写入失败" }
                output.fd.sync()
            }
            if (file.exists()) file.delete()
            check(temporary.renameTo(file)) { "壁纸缓存替换失败" }
            return CachedWallpaper(file, bitmap.width, bitmap.height)
        } finally {
            bitmap.recycle()
            if (temporary.exists()) temporary.delete()
        }
    }
}
