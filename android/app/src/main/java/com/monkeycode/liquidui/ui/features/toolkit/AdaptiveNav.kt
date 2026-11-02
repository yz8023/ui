package com.monkeycode.liquidui.ui.features.toolkit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * 自适应导航（手册 F21 / 条目 U04）：同一套路由，窄屏走底部 `NavigationBar`，
 * 宽屏走侧边 `NavigationRail`——手机、折叠屏展开态、平板、ChromeOS 窗口都不用改代码。
 *
 * **为什么不引 `material3-window-size-class`**：那个官方工件当然更标准，但要多一个依赖，
 * 而分级本身就是「量宽度 + 两个断点」。这里用 `LocalConfiguration.screenWidthDp`
 * 自己判（0 依赖），断点沿用 Material 的 600dp / 840dp。宿主想用官方版就把
 * [rememberAppWindowClass] 换成 `calculateWindowSizeClass()`，其余代码不动。
 *
 * 断点判断用**窗口宽度**而不是屏幕宽度：多窗口/分屏/桌面模式下 Activity 拿到的
 * configuration 就是窗口尺寸，这正是我们要的。
 */
enum class AppWindowClass(val label: String, val minWidthDp: Int) {
    /** < 600dp：手机竖屏。底部导航。 */
    COMPACT("紧凑", 0),

    /** 600–839dp：折叠屏展开、小平板、手机横屏。侧边栏更省纵向空间。 */
    MEDIUM("中等", 600),

    /** ≥ 840dp：平板、桌面窗口。侧边栏可带文字标签。 */
    EXPANDED("展开", 840)
}

/** 按窗口宽度分级。 */
fun windowClassOf(widthDp: Int): AppWindowClass = when {
    widthDp >= AppWindowClass.EXPANDED.minWidthDp -> AppWindowClass.EXPANDED
    widthDp >= AppWindowClass.MEDIUM.minWidthDp -> AppWindowClass.MEDIUM
    else -> AppWindowClass.COMPACT
}

/** 当前窗口宽度分级（响应式：旋转/分屏/折叠屏展开都会自动重算）。 */
@Composable
fun rememberAppWindowClass(): AppWindowClass =
    windowClassOf(LocalConfiguration.current.screenWidthDp)

/** 一个导航目的地。id 用于状态保存，label 用于无障碍与宽屏文字。 */
data class AdaptiveNavItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)

/**
 * 自适应外壳：分级为 [AppWindowClass.COMPACT] 时内容在上、底栏在下；
 * 否则侧栏在左、内容在右。内容区永远拿到整块剩余空间，不需要自己算内边距。
 *
 * @param selectedIndex 选中下标（宿主用 `rememberSaveable` 存，旋转不丢）
 * @param showLabelsOnRail 展开态是否在侧栏显示文字（中等宽度建议关掉，省地方）
 */
@Composable
fun AdaptiveAppShell(
    items: List<AdaptiveNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    windowClass: AppWindowClass = rememberAppWindowClass(),
    showLabelsOnRail: Boolean = windowClass == AppWindowClass.EXPANDED,
    content: @Composable () -> Unit
) {
    if (windowClass == AppWindowClass.COMPACT) {
        Column(modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().weight(1f)) { content() }
            AdaptiveBottomBar(items, selectedIndex, onSelect)
        }
    } else {
        Row(modifier.fillMaxSize()) {
            AdaptiveSideRail(items, selectedIndex, onSelect, showLabels = showLabelsOnRail)
            Box(Modifier.fillMaxHeight().weight(1f)) { content() }
        }
    }
}

/** 窄屏底部导航。单独暴露，方便宿主套自己的 Scaffold。 */
@Composable
fun AdaptiveBottomBar(
    items: List<AdaptiveNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, maxLines = 1) },
                alwaysShowLabel = items.size <= 4
            )
        }
    }
}

/** 宽屏侧边导航栏。单独暴露，方便宿主套自己的 Scaffold。 */
@Composable
fun AdaptiveSideRail(
    items: List<AdaptiveNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    showLabels: Boolean = true,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        Column(
            Modifier.fillMaxHeight().padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEachIndexed { index, item ->
                NavigationRailItem(
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    // 始终传 label lambda，内部按 showLabels 决定渲染与否：
                    // 条件式返回 @Composable lambda 会让类型推断变得脆弱，这样写更稳。
                    label = {
                        if (showLabels) {
                            Text(item.label, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                )
            }
        }
    }
}
