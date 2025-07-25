package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_15_0 : Updater {
    override fun registerUpdaters(context: CompoundTagUpdaterContext) {
        context.addUpdater(1, 15, 0)
            .match("name", "minecraft:kelp")
            .visit("states")
            .edit("age") { helper: CompoundTagEditHelper ->
                val age = helper.tag as Int
                helper.replaceWith("kelp_age", age)
            }
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_15_0()
    }
}
