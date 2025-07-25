package org.chorus_oss.chorus.command.selector.args.impl

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.exceptions.SelectorSyntaxException
import org.chorus_oss.chorus.command.selector.ParseUtils
import org.chorus_oss.chorus.command.selector.SelectorType
import org.chorus_oss.chorus.command.selector.args.CachedFilterSelectorArgument
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.Transform
import java.util.function.Function
import kotlin.math.abs

class C : CachedFilterSelectorArgument() {
    @Throws(SelectorSyntaxException::class)
    override fun cache(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform,
        vararg arguments: String
    ): Function<List<Entity>, List<Entity>> {
        ParseUtils.singleArgument(arguments.toList().toTypedArray(), keyName)
        ParseUtils.cannotReversed(arguments[0])
        val c = arguments[0].toInt()
        if (c == 0) throw SelectorSyntaxException("C cannot be zero!")
        return Function { entities ->
            val sorted = entities.sortedWith(Comparator.comparingDouble { e ->
                e.position.distanceSquared(
                    basePos.position
                )
            }).toMutableList()
            if (c < 0) sorted.reverse()
            sorted.subList(0, abs(c.toDouble()).toInt())
        }
    }

    override val keyName: String
        get() = "c"

    override val priority: Int
        get() = 3
}
