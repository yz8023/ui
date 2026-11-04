package com.monkeycode.liquidui.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 返回栈语义的单元测试（批 A）。
 * 重点是「操作被调用」与「状态被改变」的区别：singleTop 命中去重、单元素栈 pop、
 * popUntil 找不到目标，这三种情况栈都不该变，日志里 changed 必须是 false。
 */
class RouteStackTest {

    @Test
    fun freshStackHoldsOnlyTheRoot() {
        val stack = RouteStack("home")
        assertEquals(1, stack.depth)
        assertEquals("home", stack.current)
        assertEquals("home", stack.root)
        assertEquals(listOf("home"), stack.entries)
        assertFalse(stack.canGoBack)
        assertTrue(stack.shouldExitOnBack())
    }

    @Test
    fun popOnSingleEntryStackChangesNothing() {
        val stack = RouteStack("home")
        assertNull(stack.pop())
        assertEquals(1, stack.depth)
        assertEquals("home", stack.current)
        assertFalse("log must record that nothing changed", stack.log.last().changed)
    }

    @Test
    fun pushGrowsTheStackAndEnablesBack() {
        val stack = RouteStack("home")
        assertTrue(stack.push("detail"))
        assertEquals(2, stack.depth)
        assertEquals("detail", stack.current)
        assertEquals("home", stack.root)
        assertTrue(stack.canGoBack)
        assertFalse(stack.shouldExitOnBack())
        assertTrue(stack.push("comment"))
        assertEquals(listOf("home", "detail", "comment"), stack.entries)
    }

    @Test
    fun singleTopDeduplicatesOnlyWhenAsked() {
        val stack = RouteStack("home")
        stack.push("detail")
        assertFalse("singleTop on the same route must not push", stack.push("detail", singleTop = true))
        assertEquals(2, stack.depth)
        assertTrue("without singleTop the same route is pushed again", stack.push("detail"))
        assertEquals(3, stack.depth)
        assertEquals(listOf("home", "detail", "detail"), stack.entries)
    }

    @Test
    fun replaceSwapsTheTopAndKeepsTheRest() {
        val stack = RouteStack("home")
        stack.push("detail")
        val old = stack.replace("editor")
        assertEquals("detail", old)
        assertEquals(2, stack.depth)
        assertEquals("editor", stack.current)
        assertEquals("home", stack.root)
        assertEquals(listOf("home", "editor"), stack.entries)
    }

    @Test
    fun replaceAllRebuildsTheWholeStack() {
        val stack = RouteStack("home")
        stack.push("a")
        stack.push("b")
        assertTrue(stack.replaceAll(listOf("tab2", "x")))
        assertEquals(2, stack.depth)
        assertEquals("x", stack.current)
        assertEquals("tab2", stack.root)
    }

    @Test
    fun replaceAllRejectsAnEmptyList() {
        val stack = RouteStack("home")
        stack.push("a")
        assertFalse(stack.replaceAll(emptyList()))
        assertEquals(listOf("home", "a"), stack.entries)
        assertFalse(stack.log.last().changed)
    }

    @Test
    fun popToRootReportsHowManyWerePopped() {
        val stack = RouteStack("home")
        stack.push("a")
        stack.push("b")
        stack.push("c")
        assertEquals(3, stack.popToRoot())
        assertEquals(1, stack.depth)
        assertEquals("home", stack.current)
        assertFalse(stack.canGoBack)
        assertEquals("second popToRoot pops nothing", 0, stack.popToRoot())
    }

    @Test
    fun popUntilStopsAtTheTargetRoute() {
        val stack = RouteStack("home")
        stack.push("list")
        stack.push("detail")
        stack.push("comment")
        assertEquals(2, stack.popUntil("list"))
        assertEquals("list", stack.current)
        assertEquals(2, stack.depth)
        assertEquals(0, stack.popUntil("list"))
    }

    @Test
    fun popUntilReturnsMinusOneForUnknownRoute() {
        val stack = RouteStack("home")
        stack.push("detail")
        assertEquals(-1, stack.popUntil("nowhere"))
        assertEquals(listOf("home", "detail"), stack.entries)
        assertFalse(stack.log.last().changed)
    }

    @Test
    fun popUntilFindsTheLastOccurrence() {
        val stack = RouteStack("home")
        stack.push("detail")
        stack.push("home")
        // 栈里有两个 home，应该停在最后那个（栈顶），一个都不弹
        assertEquals(0, stack.popUntil("home"))
        assertEquals(3, stack.depth)
    }

    @Test
    fun onBackPressedPopsUntilOnlyTheRootRemains() {
        val stack = RouteStack("home")
        stack.push("a")
        stack.push("b")
        assertTrue(stack.onBackPressed())
        assertEquals("a", stack.current)
        assertTrue(stack.onBackPressed())
        assertEquals("home", stack.current)
        assertFalse("with only the root left the system should handle back", stack.onBackPressed())
        assertEquals(1, stack.depth)
    }

    @Test
    fun containsAndIndexOfReportStackContents() {
        val stack = RouteStack("home")
        stack.push("a")
        assertTrue(stack.contains("a"))
        assertFalse(stack.contains("z"))
        assertEquals(1, stack.indexOf("a"))
        assertEquals(-1, stack.indexOf("z"))
    }

    @Test
    fun entriesSnapshotCannotMutateTheStack() {
        val stack = RouteStack("home")
        stack.push("a")
        val snapshot = stack.entries
        assertEquals(2, snapshot.size)
        stack.pop()
        assertEquals("snapshot must not follow later mutations", 2, snapshot.size)
        assertEquals(1, stack.depth)
    }

    @Test
    fun logRecordsEveryOperationWithItsEffect() {
        val stack = RouteStack("home")
        stack.clearLog()
        stack.push("a")
        stack.push("a", singleTop = true)
        stack.pop()
        val log = stack.log
        assertEquals(3, log.size)
        assertEquals(RouteStack.OP_PUSH, log[0].operation)
        assertTrue(log[0].changed)
        assertEquals(2, log[0].depthAfter)
        assertEquals(RouteStack.OP_PUSH, log[1].operation)
        assertFalse(log[1].changed)
        assertEquals(2, log[1].depthAfter)
        assertEquals(RouteStack.OP_POP, log[2].operation)
        assertTrue(log[2].changed)
        assertEquals(1, log[2].depthAfter)
        stack.clearLog()
        assertTrue(stack.log.isEmpty())
    }

    @Test
    fun operationKeysAreStableEnglishIdentifiers() {
        // 数据侧存稳定英文键（批 C 定的惯例）：这些键会被当 map 键用，不能是显示文案
        assertEquals(6, RouteStack.OPERATIONS.size)
        assertTrue(RouteStack.OPERATIONS.all { it.matches(Regex("[a-z_]+")) })
        assertTrue(RouteStack.OPERATIONS.containsAll(listOf("push", "pop", "replace", "replace_all", "pop_to_root", "pop_until")))
    }
}
