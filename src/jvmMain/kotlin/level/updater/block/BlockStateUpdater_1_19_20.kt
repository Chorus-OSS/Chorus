package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_19_20 : Updater {
    override fun registerUpdaters(context: CompoundTagUpdaterContext) {
        this.addProperty(context, "minecraft:muddy_mangrove_roots", "pillar_axis", "y")
    }

    private fun addProperty(context: CompoundTagUpdaterContext, identifier: String, propertyName: String, value: Any) {
        context.addUpdater(1, 18, 10, true) // Here we go again Mojang
            .match("name", identifier)
            .visit("states")
            .tryAdd(propertyName, value)
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_19_20()
    }
}
