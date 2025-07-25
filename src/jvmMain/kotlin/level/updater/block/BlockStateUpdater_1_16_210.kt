package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_16_210 : Updater {
    override fun registerUpdaters(context: CompoundTagUpdaterContext) {
        registerUpdater(context, "minecraft:stripped_crimson_stem")
        registerUpdater(context, "minecraft:stripped_warped_stem")
        registerUpdater(context, "minecraft:stripped_crimson_hyphae")
        registerUpdater(context, "minecraft:stripped_warped_hyphae")
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_16_210()

        private fun registerUpdater(context: CompoundTagUpdaterContext, name: String) {
            context.addUpdater(1, 16, 210)
                .match("name", name)
                .visit("states")
                .remove("deprecated")
        }
    }
}
