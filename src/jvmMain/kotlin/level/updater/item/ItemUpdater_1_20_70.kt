package org.chorus_oss.chorus.level.updater.item

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class ItemUpdater_1_20_70 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        ctx.addUpdater(1, 20, 70)
            .match("Name", "minecraft:grass")
            .edit(
                "Name"
            ) { helper: CompoundTagEditHelper ->
                helper.rootTag["Name"] = "minecraft:grass_block"
            }
    }

    companion object {
        val INSTANCE: Updater = ItemUpdater_1_20_70()
    }
}
