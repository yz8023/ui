package com.monkeycode.liquidui.data

import android.content.SharedPreferences
import com.monkeycode.liquidui.domain.SettingsRules

/**
 * 数据层：本仓库唯一持有 `SharedPreferences` 的地方（批 D-架构）。
 *
 * 依赖方向：`data → domain` 允许，`data → ui` 不允许；`ui → data` 允许，`ui → SharedPreferences` 不允许。
 * **这一层不持 `Context`**。重构前 `AppSettingsState` 的构造参数写作 `private val prefs: Context`
 * ——名叫 prefs、类型却是 Context，比实际需要的宽；批 F 的 S18 把它列为清单上唯一的真疑点，
 * 批 D 在这里把类型收窄掉：状态持有者只认仓库，仓库只认 `SharedPreferences`。
 *
 * 关于「为什么不加 ViewModel」：本仓库的设置状态是应用级的，由 `rememberAppSettingsState()`
 * 配合 `applicationContext` 提供，进程重建后从 prefs 恢复，加一层 ViewModel 不解决任何现存问题。
 * 这条判断写在 `docs/开源素材清单-批G.md` §五 与 S22 页面里，不是漏做。
 */
class SettingsRepository(private val prefs: SharedPreferences) {

    fun readString(key: String, default: String): String = prefs.getString(key, default) ?: default

    fun readNullableString(key: String, default: String?): String? = prefs.getString(key, default)

    fun readBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)

    fun readFloat(key: String, default: Float): Float = prefs.getFloat(key, default)

    fun readInt(key: String, default: Int): Int = prefs.getInt(key, default)

    fun contains(key: String): Boolean = prefs.contains(key)

    fun writeString(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    fun writeBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun writeFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }

    fun writeInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    /**
     * 一次 `edit()` 写多个字符串：原子落盘，也比连续调用 [writeString] 少几次磁盘写。
     * 壁纸路径与来源 URL 必须同生共死（只写了路径没写 URL，缓存壁纸就成了来历不明的文件）。
     */
    fun writeStrings(vararg pairs: Pair<String, String?>) {
        val editor = prefs.edit()
        pairs.forEach { editor.putString(it.first, it.second) }
        editor.apply()
    }

    /** 导出主题 JSON 时用：一次性把所有键值读出来，避免边导出边被别的写入改动。 */
    fun snapshot(): Map<String, Any> {
        val all = prefs.all
        val out = LinkedHashMap<String, Any>(all.size)
        all.forEach { entry -> entry.value?.let { out[entry.key] = it } }
        return out
    }

    companion object {
        /** prefs 文件名是跨版本契约：改了老用户的设置就全丢（批 A 的 `SettingsRulesTest` 锁住了这个字面值）。 */
        const val FILE_NAME: String = SettingsRules.PREFS_FILE
    }
}
