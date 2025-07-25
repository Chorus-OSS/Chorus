package org.chorus_oss.chorus.level.updater.block

import org.chorus_oss.chorus.level.updater.Updater
import org.chorus_oss.chorus.level.updater.util.tagupdater.CompoundTagUpdaterContext

class BlockStateUpdater_1_17_30 : Updater {
    override fun registerUpdaters(context: CompoundTagUpdaterContext) {
        this.updateItemFrame("minecraft:frame", context)
        this.updateItemFrame("minecraft:glow_frame", context)
    }

    private fun updateItemFrame(name: String, context: CompoundTagUpdaterContext) {
        context.addUpdater(1, 16, 210, true) // Palette version wasn't bumped so far
            .match("name", name)
            .visit("states")
            .tryAdd("item_frame_photo_bit", 0.toByte())
    }

    companion object {
        val INSTANCE: Updater = BlockStateUpdater_1_17_30()
    }
}
