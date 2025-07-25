package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_20_60 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        ctx.addUpdater(1, 20, 60)
            .match("name", "minecraft:hard_stained_glass")
            .visit("states")
            .edit("color") { helper: CompoundTagEditHelper ->
                var color = helper.tag as String
                if (color == "silver") {
                    color = "light_gray"
                }
                helper.rootTag["name"] = "minecraft:hard_" + color + "_stained_glass"
            }
            .remove("color")

        ctx.addUpdater(1, 20, 60)
            .match("name", "minecraft:hard_stained_glass_pane")
            .visit("states")
            .edit("color") { helper: CompoundTagEditHelper ->
                var color = helper.tag as String
                if (color == "silver") {
                    color = "light_gray"
                }
                helper.rootTag["name"] = "minecraft:hard_" + color + "_stained_glass_pane"
            }
            .remove("color")
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_20_60()
    }
}
