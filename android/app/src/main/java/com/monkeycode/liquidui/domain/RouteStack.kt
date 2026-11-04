package com.monkeycode.liquidui.domain

/**
 * 返回栈的操作语义（纯 Kotlin，零 Android/Compose 依赖 → 可在 JVM 上被单元测试覆盖）。
 *
 * 取材边界：push/pop/replace/replaceAll/popToRoot/popUntil 的语义与 singleTop 去重取自
 * adrielcafe/voyager（MIT）的 Navigator 口径。**不做导航控件**——既有素材里的「抽屉导航」是控件形态，
 * 这一层只管栈这个数据结构在每种操作后应该变成什么样，以及返回键什么时候该退出。
 *
 * 路由用稳定英文键（如 "home"／"detail"），不是显示文案；显示文案由 UI 层查资源。
 */
class RouteStack(root: String) {

    private val stack = mutableListOf(root)

    /** 每次操作追加一条日志，S21 直接把它渲染成操作历史（键，不是文案）。 */
    private val logEntries = mutableListOf<StackLogEntry>()

    /** 栈内容（快照，改不动内部）。 */
    val entries: List<String> get() = stack.toList()

    /** 操作日志（快照）。 */
    val log: List<StackLogEntry> get() = logEntries.toList()

    val depth: Int get() = stack.size
    val current: String get() = stack.last()
    val root: String get() = stack.first()
    val canGoBack: Boolean get() = stack.size > 1

    /**
     * 入栈。
     * @param singleTop 为真且目标已等于栈顶时不入栈（去重），返回 false 表示「栈没变」。
     */
    fun push(route: String, singleTop: Boolean = false): Boolean {
        if (singleTop && stack.last() == route) {
            logEntries.add(StackLogEntry(OP_PUSH, route, false, depth))
            return false
        }
        stack.add(route)
        logEntries.add(StackLogEntry(OP_PUSH, route, true, depth))
        return true
    }

    /**
     * 出栈。**最后一个元素不出**：栈底是根路由，弹掉它就等于退出页面，
     * 这个决定权属于调用方（见 [shouldExitOnBack]），不属于栈自己。
     * @return 被弹出的路由；栈只剩一个元素时返回 null 且栈不变。
     */
    fun pop(): String? {
        if (stack.size <= 1) {
            logEntries.add(StackLogEntry(OP_POP, stack.last(), false, depth))
            return null
        }
        val removed = stack.removeAt(stack.lastIndex)
        logEntries.add(StackLogEntry(OP_POP, removed, true, depth))
        return removed
    }

    /** 换掉栈顶（栈深不变）。@return 被换掉的路由。 */
    fun replace(route: String): String {
        val old = stack.removeAt(stack.lastIndex)
        stack.add(route)
        logEntries.add(StackLogEntry(OP_REPLACE, route, true, depth))
        return old
    }

    /** 整栈重建（例如切主 Tab 时把栈换成「根 + 目标」）。栈深变为 routes.size。 */
    fun replaceAll(routes: List<String>): Boolean {
        if (routes.isEmpty()) {
            logEntries.add(StackLogEntry(OP_REPLACE_ALL, "", false, depth))
            return false
        }
        stack.clear()
        stack.addAll(routes)
        logEntries.add(StackLogEntry(OP_REPLACE_ALL, routes.last(), true, depth))
        return true
    }

    /** 弹到只剩栈底。@return 弹掉了几个。 */
    fun popToRoot(): Int {
        var popped = 0
        while (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
            popped++
        }
        logEntries.add(StackLogEntry(OP_POP_TO_ROOT, root, popped > 0, depth))
        return popped
    }

    /**
     * 弹到指定路由为止（该路由成为新栈顶）。
     * @return 弹掉了几个；栈里没有这个路由时返回 −1 且栈不变。
     */
    fun popUntil(route: String): Int {
        val index = stack.lastIndexOf(route)
        if (index < 0) {
            logEntries.add(StackLogEntry(OP_POP_UNTIL, route, false, depth))
            return -1
        }
        val popped = stack.lastIndex - index
        while (stack.lastIndex > index) stack.removeAt(stack.lastIndex)
        logEntries.add(StackLogEntry(OP_POP_UNTIL, route, popped > 0, depth))
        return popped
    }

    /** 栈里是否已有该路由（判断要不要用 singleTop 或 popUntil）。 */
    fun contains(route: String): Boolean = stack.contains(route)

    /** 该路由在栈里的最后一次出现位置；不存在返回 −1。 */
    fun indexOf(route: String): Int = stack.lastIndexOf(route)

    /**
     * 返回键该怎么处理：栈深大于 1 就弹一层，否则该交回系统（退出页面／退出应用）。
     * 这条判定是 BackHandler(enabled = canGoBack) 的依据——**enabled 必须跟着栈深走**，
     * 栈深为 1 时还吃返回键，用户就退不出去了。
     */
    fun shouldExitOnBack(): Boolean = stack.size <= 1

    /** 处理一次返回键。@return true 表示栈自己消化了（弹了一层），false 表示该交回系统。 */
    fun onBackPressed(): Boolean {
        if (shouldExitOnBack()) return false
        pop()
        return true
    }

    fun clearLog() {
        logEntries.clear()
    }

    companion object {
        const val OP_PUSH = "push"
        const val OP_POP = "pop"
        const val OP_REPLACE = "replace"
        const val OP_REPLACE_ALL = "replace_all"
        const val OP_POP_TO_ROOT = "pop_to_root"
        const val OP_POP_UNTIL = "pop_until"

        /** 全部操作键，UI 层按这个顺序渲染按钮、按键查资源拿显示名。 */
        val OPERATIONS: List<String> = listOf(
            OP_PUSH, OP_POP, OP_REPLACE, OP_REPLACE_ALL, OP_POP_TO_ROOT, OP_POP_UNTIL,
        )
    }
}

/**
 * 一条操作日志。
 * @param changed 栈有没有真的变化（singleTop 命中去重、单元素栈 pop、popUntil 找不到都是 false）——
 *                「操作被调用」与「状态被改变」是两件事，S21 专门把这一列显示出来。
 */
data class StackLogEntry(
    val operation: String,
    val route: String,
    val changed: Boolean,
    val depthAfter: Int,
)
