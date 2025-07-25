package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.OrderedUpdater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagEditHelper
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_20_40 : Updater {
    override fun registerUpdaters(ctx: CompoundTagUpdaterContext) {
        this.addDirectionUpdater(ctx, "minecraft:chest", OrderedUpdater.Companion.FACING_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:ender_chest", OrderedUpdater.Companion.FACING_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:stonecutter_block", OrderedUpdater.Companion.FACING_TO_CARDINAL)
        this.addDirectionUpdater(ctx, "minecraft:trapped_chest", OrderedUpdater.Companion.FACING_TO_CARDINAL)
    }

    private fun addDirectionUpdater(ctx: CompoundTagUpdaterContext, identifier: String, updater: OrderedUpdater) {
        ctx.addUpdater(1, 20, 40)
            .match("name", identifier)
            .visit("states")
            .edit(updater.oldProperty) { helper: CompoundTagEditHelper ->
                val value = helper.tag as Int
                helper.replaceWith(updater.newProperty, updater.translate(value))
            }
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_20_40()
    }
}
