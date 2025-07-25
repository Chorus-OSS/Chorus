package org.chorus_oss.chorus.command.selector.args.impl

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.exceptions.SelectorSyntaxException
import org.chorus_oss.chorus.command.selector.ParseUtils
import org.chorus_oss.chorus.command.selector.SelectorType
import org.chorus_oss.chorus.command.selector.args.CachedSimpleSelectorArgument
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.Transform
import java.util.function.Predicate

class RXM : CachedSimpleSelectorArgument() {
    @Throws(SelectorSyntaxException::class)
    override fun cache(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform?,
        vararg arguments: String
    ): Predicate<Entity> {
        ParseUtils.singleArgument(arguments.toList().toTypedArray(), keyName)
        ParseUtils.cannotReversed(arguments[0])
        val rxm = arguments[0].toDouble()
        if (!ParseUtils.checkBetween(
                -90.0,
                90.0,
                rxm
            )
        ) throw SelectorSyntaxException("RXM out of bound (-90 - 90): $rxm")
        return Predicate { entity: Entity -> entity.rotation.pitch >= rxm }
    }

    override val keyName: String
        get() = "rxm"

    override val priority: Int
        get() = 3
}
