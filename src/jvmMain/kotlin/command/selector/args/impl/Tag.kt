package org.chorus_oss.chorus.command.selector.args.impl

import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.selector.ParseUtils
import org.chorus_oss.chorus.command.selector.SelectorType
import org.chorus_oss.chorus.command.selector.args.CachedSimpleSelectorArgument
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.level.Transform
import java.util.function.Predicate

class Tag : CachedSimpleSelectorArgument() {
    override fun cache(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform?,
        vararg arguments: String
    ): Predicate<Entity> {
        val have = ArrayList<String>()
        val dontHave = ArrayList<String>()
        for (tag in arguments) {
            var tag1 = tag
            val reversed: Boolean = ParseUtils.checkReversed(tag1)
            if (reversed) {
                tag1 = tag1.substring(1)
                dontHave.add(tag1)
            } else have.add(tag1)
        }
        return Predicate { entity: Entity ->
            have.stream().allMatch { tag -> entity.containTag(tag) } && dontHave.stream()
                .noneMatch { tag -> entity.containTag(tag) }
        }
    }

    override val keyName: String
        get() = "tag"

    override val priority: Int
        get() = 4
}
