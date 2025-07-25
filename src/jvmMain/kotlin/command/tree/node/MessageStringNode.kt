package org.chorus_oss.chorus.command.tree.node

import org.chorus_oss.chorus.command.exceptions.SelectorSyntaxException
import org.chorus_oss.chorus.command.selector.EntitySelectorAPI
import java.util.*
import java.util.regex.MatchResult
import java.util.regex.Matcher
import kotlin.math.max
import kotlin.math.min

/**
 * 解析全部剩余参数拼接为`String`值
 *
 *
 * 所有命令参数类型为[MESSAGE][org.chorus_oss.chorus.command.data.CommandParamType.MESSAGE]
 * 如果没有手动指定[IParamNode],则会默认使用这个解析
 */
class MessageStringNode : ParamNode<String?>() {
    private val TMP: MutableList<String> = ArrayList()

    override fun fill(arg: String) {
        if (paramList.index != paramList.paramTree.args.size) TMP.add(arg)
        else {
            TMP.add(arg)

            val str = java.lang.String.join(" ", TMP)
            val match: Matcher = EntitySelectorAPI.Companion.ENTITY_SELECTOR.matcher(str)
            this.value = match.replaceAll { r: MatchResult? ->
                val start = max(0.0, (match.start() - 1).toDouble()).toInt()
                val end = min(str.length.toDouble(), match.end().toDouble()).toInt()
                if (start != 0) {
                    val before = str[start]
                    if (before == '”' || before == '\'' || before == '\\' || before == ';') return@replaceAll match.group()
                }
                if (end != str.length) {
                    val after = str[end]
                    if (after == '”' || after == '\'' || after == '\\' || after == ';') return@replaceAll match.group()
                }
                val m = match.group()
                if (EntitySelectorAPI.Companion.api.checkValid(m)) {
                    val join = StringJoiner(", ")
                    try {
                        for (entity in EntitySelectorAPI.Companion.api
                            .matchEntities(paramList.paramTree.sender!!, m)) {
                            var name = entity.getEntityName()
                            if (name.isBlank()) name = entity.getOriginalName()
                            join.add(name)
                        }
                    } catch (e: SelectorSyntaxException) {
                        error(e.message)
                    }
                    return@replaceAll join.toString()
                } else {
                    return@replaceAll m
                }
            }
        }
    }

    override fun reset() {
        super.reset()
        TMP.clear()
    }
}
