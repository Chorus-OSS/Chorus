package org.chorus_oss.chorus.command.selector

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.command.exceptions.SelectorSyntaxException

/**
 * 一些有关目标选择器解析的常用静态函数
 */
object ParseUtils {
    /**
     * 解析偏移int值
     * @param value 文本
     * @param base 基础值
     * @return 偏移值
     */
    @Throws(SelectorSyntaxException::class)
    fun parseOffsetInt(value: String, base: Int): Int {
        try {
            return if (value.startsWith("~")) {
                if (value.length != 1) base + value.substring(1).toInt() else base
            } else {
                value.toInt()
            }
        } catch (e: NumberFormatException) {
            throw SelectorSyntaxException("Error parsing target selector", e)
        }
    }

    /**
     * 解析偏移double值
     * @param value 文本
     * @param base 基础值
     * @return 偏移值
     */
    @Throws(SelectorSyntaxException::class)
    fun parseOffsetDouble(value: String, base: Double): Double {
        try {
            return if (value.startsWith("~")) {
                if (value.length != 1) base + value.substring(1).toDouble() else base
            } else {
                value.toDouble()
            }
        } catch (e: NumberFormatException) {
            throw SelectorSyntaxException("Error parsing target selector", e)
        }
    }

    /**
     * 检查参数是否反转
     * @param value 给定字符串
     * @return 是否反转
     */
    fun checkReversed(value: String): Boolean {
        return value.startsWith("!")
    }

    /**
     * 要求参数不能取反。若取反，则抛出[SelectorSyntaxException]
     * @param value 给定字符串
     */
    @Throws(SelectorSyntaxException::class)
    fun cannotReversed(value: String) {
        if (checkReversed(value)) throw SelectorSyntaxException("Argument cannot be reversed!")
    }

    /**
     * 要求参数不能多于1
     * @param args 参数列表
     * @param keyName 参数键名
     */
    @Throws(SelectorSyntaxException::class)
    fun singleArgument(args: Array<String>, keyName: String) {
        if (args.size > 1) throw SelectorSyntaxException("Multiple arguments is not allow in arg $keyName")
    }

    /**
     * 检查给定值是否在给定的两个数之间
     * @param bound1 边界1
     * @param bound2 边界2
     * @param value 之值
     * @return 给定值是否在给定的两个数之间
     */
    fun checkBetween(bound1: Double, bound2: Double, value: Double): Boolean {
        return if (bound1 < bound2) (value in bound1..bound2) else (value in bound2..bound1)
    }

    /**
     * 通过给定游戏模式字符串解析游戏模式数字
     *
     *
     * 此方法可匹配参数与原版选择器参数m给定预定值相同
     * @param token 字符串
     * @return 游戏模式数字
     */
    @Throws(SelectorSyntaxException::class)
    fun parseGameMode(token: String): Int {
        return when (token) {
            "s", "survival", "0" -> 0
            "c", "creative", "1" -> 1
            "a", "adventure", "2" -> 2
            "spectator", "3" -> 3
            "d", "default" -> Server.instance.defaultGamemode
            else -> throw SelectorSyntaxException("Unknown gamemode token: $token")
        }
    }
}
