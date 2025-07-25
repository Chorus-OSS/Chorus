package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_13_0 : Updater {
    override fun registerUpdaters(context: CompoundTagUpdaterContext) {
        context.addUpdater(1, 13, 0)
            .match("name", "minecraft:lever")
            .visit("states")
            .edit("facing_direction") { helper: CompoundTagEditHelper ->
                val value = helper.tag as Int
                helper.replaceWith("lever_direction", LEVER_DIRECTIONS[value])
            }

        registerLogUpdater("minecraft:log", "old_log_type", context)
        registerLogUpdater("minecraft:log2", "new_log_type", context)

        registerPillarUpdater("minecraft:log", context)
        registerPillarUpdater("minecraft:quartz_block", context)
        registerPillarUpdater("minecraft:log2", context)
        registerPillarUpdater("minecraft:purpur_block", context)
        registerPillarUpdater("minecraft:bone_block", context)
        registerPillarUpdater("minecraft:stripped_spruce_log", context)
        registerPillarUpdater("minecraft:stripped_birch_log", context)
        registerPillarUpdater("minecraft:stripped_jungle_log", context)
        registerPillarUpdater("minecraft:stripped_acacia_log", context)
        registerPillarUpdater("minecraft:stripped_dark_oak_log", context)
        registerPillarUpdater("minecraft:stripped_oak_log", context)
        registerPillarUpdater("minecraft:wood", context)
        registerPillarUpdater("minecraft:hay_block", context)

        context.addUpdater(1, 13, 0)
            .match("name", "minecraft:end_rod")
            .visit("states")
            .regex("facing_direction", "[^0-5]")
            .remove("facing_direction")
            .addInt("block_light_level", 14)
            .popVisit()
            .edit(
                "name"
            ) { helper: CompoundTagEditHelper -> helper.replaceWith("name", "minecraft:light_block") }

        context.addUpdater(1, 13, 0)
            .regex("name", "minecraft:.+")
            .visit("states")
            .edit("facing_direction") { helper: CompoundTagEditHelper ->
                val value = helper.tag as Int
                if (value >= 6) {
                    helper.replaceWith("facing_direction", 0)
                }
            }

        context.addUpdater(1, 13, 0)
            .regex("name", "minecraft:.+")
            .visit("states")
            .edit("fill_level") { helper: CompoundTagEditHelper ->
                val value = helper.tag as Int
                if (value >= 7) {
                    helper.replaceWith("fill_level", 6)
                }
            }
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_13_0()

        private val LEVER_DIRECTIONS = arrayOf(
            "down_east_west",
            "east",
            "west",
            "south",
            "north",
            "up_north_south",
            "up_east_west",
            "down_north_south"
        )
        private val PILLAR_DIRECTION = arrayOf("y", "x", "z")

        private fun registerLogUpdater(name: String, replace: String, context: CompoundTagUpdaterContext) {
            context.addUpdater(1, 13, 0)
                .match("name", name)
                .visit("states")
                .regex("direction", "[0-2]")
                .edit("direction") { helper: CompoundTagEditHelper ->
                    val value = helper.tag as Int
                    helper.replaceWith("pillar_axis", PILLAR_DIRECTION[value % 3])
                }

            context.addUpdater(1, 13, 0)
                .match("name", name)
                .visit("states")
                .regex("direction", "[3]")
                .rename(replace, "wood_type")
                .edit("direction") { helper: CompoundTagEditHelper ->
                    val value = helper.tag as Int
                    helper.replaceWith("pillar_axis", PILLAR_DIRECTION[value % 3])
                }
                .addByte("stripped_bit", 0.toByte())
                .popVisit()
                .edit(
                    "name"
                ) { helper: CompoundTagEditHelper ->
                    helper.replaceWith("name", "minecraft:wood")
                }
        }

        private fun registerPillarUpdater(name: String, context: CompoundTagUpdaterContext) {
            context.addUpdater(1, 13, 0)
                .match("name", name)
                .visit("states")
                .edit("direction") { helper: CompoundTagEditHelper ->
                    val value = helper.tag as Int
                    helper.replaceWith("pillar_axis", PILLAR_DIRECTION[value % 3])
                }

            context.addUpdater(1, 13, 0)
                .match("name", name)
                .visit("states")
                .tryAdd("pillar_axis", PILLAR_DIRECTION[0])
        }
    }
}
