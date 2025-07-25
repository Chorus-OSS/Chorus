package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_19_70 : Updater {
    override fun registerUpdaters(context: CompoundTagUpdaterContext) {
        for (color in COLORS) {
            this.addColorUpdater(context, color)
        }
    }

    private fun addColorUpdater(context: CompoundTagUpdaterContext, color: String) {
        context.addUpdater(1, 19, 70)
            .match("name", "minecraft:wool")
            .visit("states")
            .match("color", color)
            .edit("color") { helper: CompoundTagEditHelper ->
                if (color == "silver") {
                    helper.rootTag["name"] = "minecraft:light_gray_wool"
                } else {
                    helper.rootTag["name"] = "minecraft:" + color + "_wool"
                }
            }
            .remove("color")
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_19_70()

        val COLORS: Array<String> = arrayOf(
            "magenta",
            "pink",
            "green",
            "lime",
            "yellow",
            "black",
            "light_blue",
            "brown",
            "cyan",
            "orange",
            "red",
            "gray",
            "white",
            "blue",
            "purple",
            "silver"
        )
    }
}
