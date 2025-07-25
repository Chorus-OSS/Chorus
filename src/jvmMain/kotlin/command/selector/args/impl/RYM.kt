package org.chorus_oss.chorus.command.selector.args.impl

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.exceptions.SelectorSyntaxException
import org.chorus_oss.chorus.command.selector.ParseUtils
import org.chorus_oss.chorus.command.selector.SelectorType
import org.chorus_oss.chorus.command.selector.args.CachedSimpleSelectorArgument
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.Transform
import java.util.function.Predicate

class RYM : CachedSimpleSelectorArgument() {
    @Throws(SelectorSyntaxException::class)
    override fun cache(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform?,
        vararg arguments: String
    ): Predicate<Entity> {
        ParseUtils.singleArgument(arguments.toList().toTypedArray(), keyName)
        ParseUtils.cannotReversed(arguments[0])
        val rym = arguments[0].toDouble()
        if (!ParseUtils.checkBetween(
                -180.0,
                180.0,
                rym
            )
        ) throw SelectorSyntaxException("RX out of bound (-180 - 180): $rym")
        return Predicate { entity: Entity -> ((entity.rotation.yaw + 90) % 360 - 180) >= rym }
    }

    override val keyName: String
        get() = "rym"

    override val priority: Int
        get() = 3
}
