package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_19_80 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        // It could be done the more clever way, but Mojang added 12 updaters, and so we do.
        for (i in WOOD.indices) {
            val type = WOOD[i]
            this.addTypeUpdater(ctx, "minecraft:fence", "wood_type", type, "minecraft:" + type + "_fence")
            if (i < 4) {
                this.addTypeUpdater(ctx, "minecraft:log", "old_log_type", type, "minecraft:" + type + "_log")
            } else {
                this.addTypeUpdater(ctx, "minecraft:log2", "new_log_type", type, "minecraft:" + type + "_log")
            }
        }
    }

    private fun addTypeUpdater(
        context: CompoundTagUpdaterContext,
        identifier: String,
        typeState: String,
        type: String,
        newIdentifier: String
    ) {
        context.addUpdater(1, 19, 80)
            .match("name", identifier)
            .visit("states")
            .match(typeState, type)
            .edit(
                typeState
            ) { helper: CompoundTagEditHelper -> helper.rootTag["name"] = newIdentifier }
            .remove(typeState)
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_19_80()

        private val WOOD = arrayOf(
            "birch",
            "oak",
            "jungle",
            "spruce",
            "acacia",
            "dark_oak"
        )
    }
}
