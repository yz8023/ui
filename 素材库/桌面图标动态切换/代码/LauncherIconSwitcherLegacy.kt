/*
 * 桌面图标动态切换（App Icon Switcher）
 *
 * 功能：通过 activity-alias 在两种桌面图标（经典图标 / 新图标）之间动态切换
 * 来源：参考 CYQawa/YunX ThemeScreen.kt 桌面图标区块 + AndroidManifest.xml
 * 适用：任意 Android 项目（无需 Compose，原生 Java/Kotlin 即可）
 *
 * ─── 集成步骤 ──────────────────────────────────────────
 * 1. AndroidManifest.xml：添加 activity-alias 条目
 * 2. 资源：准备 icon.png 和 icon2.png（两套图标前景）
 * 3. 代码：集成 IconSwitcher（Compose）或 IconSwitchHelper（原生）
 */

package com.example.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

// ============================================================
// 方案 A：Compose UI 组件（推荐，与 Material 3 主题页集成）
// ============================================================

/**
 * Compose 桌面图标选择器
 *
 * 用法：
 * ```kotlin
 * IconSwitcher(
 *     currentVariant = 0,
 *     onVariantChanged = { variant -> /* 持久化 + 切换图标 */ }
 * )
 * ```
 *
 * 变体值：0 = 经典图标，1 = 新图标
 */
@Composable
fun 桌面图标切换器(
    currentVariant: Int,
    onVariantChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = "桌面图标",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            IconVariantOption(
                iconRes     = R.drawable.ic_launcher,
                label       = "经典图标",
                isSelected  = currentVariant == 0,
                onClick     = { onVariantChanged(0) }
            )
            IconVariantOption(
                iconRes     = R.drawable.ic_launcher2,
                label       = "新图标",
                isSelected  = currentVariant == 1,
                onClick     = { onVariantChanged(1) }
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = "Android 12+ 立即生效；部分设备需回到桌面或重启启动器后查看。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

/** 单个图标选项（图标预览 + 名称 + 选中角标） */
@Composable
private fun IconVariantOption(
    iconRes: Int, label: String, isSelected: Boolean, onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp))
            )
            // 选中角标（右上角对勾 + 主题色圆底）
            if (isSelected) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).size(20.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "已选择",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ============================================================
// 方案 B：原生 Kotlin/Java 工具类（无需 Compose）
// ============================================================

/**
 * 图标切换器（原生 API，可在任意 Context 中调用）
 */
object 桌面图标切换助手 {

    private const val VARIANT_CLASSIC = 0
    private const val VARIANT_NEW     = 1

    /**
     * 切换桌面图标
     * @param context   任意 Context
     * @param variant   0 = 经典图标，1 = 新图标
     * @param mainActivityClassName   主 Activity 完整类名，默认 "com.example.app.MainActivity"
     * @param aliasActivityClassName  别名 Activity 完整类名，默认 "com.example.app.MainActivityIcon2"
     */
    fun switch(
        context: Context,
        variant: Int,
        mainActivityClassName: String  = "${context.packageName}.MainActivity",
        aliasActivityClassName: String = "${context.packageName}.MainActivityIcon2"
    ) {
        val pm = context.packageManager
        val main = ComponentName(context, mainActivityClassName)
        val alias = ComponentName(context, aliasActivityClassName)

        if (variant == VARIANT_NEW) {
            // 启用新图标（alias），禁用经典图标（main）
            pm.setComponentEnabledSetting(
                alias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,  PackageManager.DONT_KILL_APP)
            pm.setComponentEnabledSetting(
                main,  PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        } else {
            // 启用经典图标（main），禁用新图标（alias）
            pm.setComponentEnabledSetting(
                main,  PackageManager.COMPONENT_ENABLED_STATE_ENABLED,  PackageManager.DONT_KILL_APP)
            pm.setComponentEnabledSetting(
                alias, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        }
    }

    /**
     * 读取当前保存的图标变体（从 SharedPreferences）
     */
    fun getSavedVariant(context: Context, key: String = "app_icon_variant"): Int =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .getInt(key, VARIANT_CLASSIC)

    /**
     * 保存图标变体到 SharedPreferences
     */
    fun saveVariant(context: Context, variant: Int, key: String = "app_icon_variant") {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putInt(key, variant.coerceIn(VARIANT_CLASSIC, VARIANT_NEW)).apply()
    }

    /**
     * 一键切换：保存 + 执行切换
     */
    fun switchAndSave(context: Context, variant: Int) {
        saveVariant(context, variant)
        switch(context, variant)
    }
}
