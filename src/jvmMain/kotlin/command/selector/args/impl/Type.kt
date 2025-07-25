package org.chorus_oss.chorus.command.selector.args.impl

import com.google.common.collect.ImmutableMap
import org.chorus_oss.chorus.Player
import org.chorus_oss.chorus.command.CommandSender
import org.chorus_oss.chorus.command.selector.ParseUtils
import org.chorus_oss.chorus.command.selector.SelectorType
import org.chorus_oss.chorus.command.selector.args.CachedSimpleSelectorArgument
import org.chorus_oss.chorus.entity.Entity
import org.chorus_oss.chorus.entity.custom.CustomEntity
import org.chorus_oss.chorus.level.Transform
import org.chorus_oss.chorus.registry.Registries
import java.util.function.Predicate

class Type : CachedSimpleSelectorArgument() {
    override fun cache(
        selectorType: SelectorType?,
        sender: CommandSender?,
        basePos: Transform?,
        vararg arguments: String
    ): Predicate<Entity> {
        val have = ArrayList<String>()
        val dontHave = ArrayList<String>()
        for (type in arguments) {
            var type1 = type
            val reversed: Boolean = ParseUtils.checkReversed(type1)
            if (reversed) {
                type1 = completionPrefix(type1.substring(1))
                dontHave.add(type1)
            } else have.add(completionPrefix(type1))
        }
        return Predicate { entity: Entity ->
            have.stream().allMatch { type: String -> isType(entity, type) } && dontHave.stream()
                .noneMatch { type: String -> isType(entity, type) }
        }
    }

    override fun getDefaultValue(
        values: Map<String, List<String>>,
        selectorType: SelectorType,
        sender: CommandSender?
    ): String? {
        return if (selectorType == SelectorType.RANDOM_PLAYER) "minecraft:player" else null
    }

    override val keyName: String
        get() = "type"

    override val priority: Int
        get() = 4

    protected fun completionPrefix(type: String): String {
        val completed = if (type.startsWith("minecraft:")) type else "minecraft:$type"
        if (!ENTITY_TYPE2ID.containsKey(type) && !ENTITY_TYPE2ID.containsKey(completed)) {
            //是自定义生物，不需要补全
            return type
        }
        return completed
    }

    protected fun isType(entity: Entity, type: String): Boolean {
        return if (entity is Player)  //player需要特判，因为EntityHuman的getNetworkId()返回-1
            type == "minecraft:player"
        else if (entity is CustomEntity) entity.getEntityIdentifier() == type
        else ENTITY_TYPE2ID.containsKey(type) && entity.getNetworkID() == ENTITY_TYPE2ID[type]
    }

    companion object {
        val ENTITY_ID2TYPE: Map<Int, String> = Registries.ENTITY.entityId2NetworkIdMap
        val ENTITY_TYPE2ID: Map<String, Int>

        init {
            val builder = ImmutableMap.builder<String, Int>()
            ENTITY_ID2TYPE.forEach { (id: Int?, name: String?) -> builder.put(name, id) }
            ENTITY_TYPE2ID = builder.build()
        }
    }
}
