package org.chorus_oss.chorus.command.selector.args.impl

import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.exceptions.SelectorSyntaxException
import org.chorus_oss.chorus.command.selector.ParseUtils
import org.chorus_oss.chorus.command.selector.SelectorType
import org.chorus_oss.chorus.command.selector.args.CachedSimpleSelectorArgument
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.Transform
import java.util.function.Predicate

class L : CachedSimpleSelectorArgument() {
    @Throws(SelectorSyntaxException::class)
    override fun cache(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform?,
        vararg arguments: String
    ): Predicate<Entity> {
        ParseUtils.singleArgument(arguments.toList().toTypedArray(), keyName)
        ParseUtils.cannotReversed(arguments[0])
        val l = arguments[0].toInt()
        return Predicate { entity: Entity -> entity is Player && entity.experienceLevel <= l }
    }

    override val keyName: String
        get() = "l"

    override val priority: Int
        get() = 3
}
