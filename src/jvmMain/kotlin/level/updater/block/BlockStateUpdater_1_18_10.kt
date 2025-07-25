package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_18_10 : Updater {
    override fun registerUpdaters(context: CompoundTagUpdaterContext) {
        context.addUpdater(1, 18, 10)
            .match("name", "minecraft:skull")
            .visit("states")
            .remove("no_drop_bit")


        context.addUpdater(1, 18, 10)
            .match("name", "minecraft:glow_lichen")
            .visit("states")
            .tryEdit("multi_face_direction_bits") { helper: CompoundTagEditHelper ->
                var bits = helper.tag as Int
                val north = (bits and (1 shl 2)) != 0
                val south = (bits and (1 shl 3)) != 0
                val west = (bits and (1 shl 4)) != 0
                bits = if (north) {
                    bits or (1 shl 4)
                } else {
                    bits and (1 shl 4).inv()
                }
                bits = if (south) {
                    bits or (1 shl 2)
                } else {
                    bits and (1 shl 2).inv()
                }
                bits = if (west) {
                    bits or (1 shl 3)
                } else {
                    bits and (1 shl 3).inv()
                }
                helper.replaceWith("multi_face_direction_bits", bits)
            }
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_18_10()
    }
}
