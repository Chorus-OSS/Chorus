package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_14_0 : Updater {
    override fun registerUpdaters(context: CompoundTagUpdaterContext) {
        context.addUpdater(1, 14, 0)
            .match("name", "minecraft:frame")
            .visit("states")
            .edit("weirdo_direction") { helper: CompoundTagEditHelper ->
                val tag = helper.tag as Int
                val newDirection = convertWeirdoDirectionToFacing(tag)
                helper.replaceWith("facing_direction", newDirection)
            }

        addRailUpdater("minecraft:golden_rail", context)
        addRailUpdater("minecraft:detector_rail", context)
        addRailUpdater("minecraft:activator_rail", context)

        addMaxStateUpdater("minecraft:rail", "rail_direction", 9, context)
        addMaxStateUpdater("minecraft:cake", "bite_counter", 6, context)
        addMaxStateUpdater("minecraft:chorus_flower", "age", 5, context)
        addMaxStateUpdater("minecraft:cocoa", "age", 2, context)
        addMaxStateUpdater("minecraft:composter", "composter_fill_level", 8, context)
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_14_0()

        private fun addRailUpdater(name: String, context: CompoundTagUpdaterContext) {
            context.addUpdater(1, 14, 0)
                .match("name", name)
                .visit("states")
                .edit("rail_direction") { helper: CompoundTagEditHelper ->
                    var direction = helper.tag as Int
                    if (direction > 5) direction = 0
                    helper.replaceWith("rail_direction", direction)
                }
        }

        fun addMaxStateUpdater(name: String, state: String, maxValue: Int, context: CompoundTagUpdaterContext) {
            context.addUpdater(1, 14, 0)
                .match("name", name)
                .visit("states")
                .edit(state) { helper: CompoundTagEditHelper ->
                    var value = helper.tag as Int
                    if (value > maxValue) value = maxValue
                    helper.replaceWith(state, value)
                }
        }

        private fun convertWeirdoDirectionToFacing(weirdoDirection: Int): Int {
            return when (weirdoDirection) {
                0 -> 5
                1 -> 4
                2 -> 3
                3 -> 2
                else -> 2
            }
        }
    }
}
