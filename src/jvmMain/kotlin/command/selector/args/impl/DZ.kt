package org.chorus_oss.chorus.command.selector.args.impl

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.exceptions.SelectorSyntaxException
import org.chorus_oss.chorus.command.selector.ParseUtils
import org.chorus_oss.chorus.command.selector.SelectorType
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.Transform
import java.util.function.Predicate

class DZ : ScopeArgument() {
    @Throws(SelectorSyntaxException::class)
    override fun getPredicate(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform,
        vararg arguments: String
    ): Predicate<Entity> {
        ParseUtils.singleArgument(arguments.toList().toTypedArray(), keyName)
        ParseUtils.cannotReversed(arguments[0])
        val z = basePos.z
        val dz = arguments[0].toDouble()
        return Predicate { entity: Entity -> ParseUtils.checkBetween(z, z + dz, entity.getZ()) }
    }

    override val keyName: String
        get() = "dz"
}
