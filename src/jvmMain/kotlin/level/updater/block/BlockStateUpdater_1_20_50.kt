package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_20_50 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        ctx.addUpdater(1, 20, 50)
            .match("name", "minecraft:planks")
            .visit("states")
            .edit("wood_type") { helper: CompoundTagEditHelper ->
                val type = helper.tag as String
                helper.rootTag["name"] = "minecraft:" + type + "_planks"
            }
            .remove("wood_type")

        ctx.addUpdater(1, 20, 50)
            .match("name", "minecraft:stone")
            .visit("states")
            .edit("stone_type") { helper: CompoundTagEditHelper ->
                var type = helper.tag as String
                when (type) {
                    "andesite_smooth" -> type = "polished_andesite"
                    "diorite_smooth" -> type = "polished_diorite"
                    "granite_smooth" -> type = "polished_granite"
                }
                helper.rootTag["name"] = "minecraft:$type"
            }
            .remove("stone_type")
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_20_50()
    }
}
