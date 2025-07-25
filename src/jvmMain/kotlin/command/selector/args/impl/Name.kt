package org.chorus_oss.chorus.command.selector.args.impl

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.selector.ParseUtils
import org.chorus_oss.chorus.command.selector.SelectorType
import org.chorus_oss.chorus.command.selector.args.CachedSimpleSelectorArgument
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.Transform
import java.util.function.Predicate

class Name : CachedSimpleSelectorArgument() {
    override fun cache(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform?,
        vararg arguments: String
    ): Predicate<Entity> {
        val have = ArrayList<String>()
        val dontHave = ArrayList<String>()
        for (name in arguments) {
            var name1 = name
            val reversed: Boolean = ParseUtils.checkReversed(name1)
            if (reversed) {
                name1 = name1.substring(1)
                dontHave.add(name1)
            } else have.add(name1)
        }
        return Predicate { entity: Entity ->
            have.stream().allMatch { name: String -> entity.getEntityName() == name } && dontHave.stream()
                .noneMatch { name: String -> entity.getEntityName() == name }
        }
    }

    override val keyName: String
        get() = "name"

    override val priority: Int
        get() = 4
}
