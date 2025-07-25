package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.redstone.RedstoneUpdateEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.item.ItemBlock
import org.chorus_oss.chorus.level.Level

class BlockLitRedstoneLamp @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockRedstoneLamp(blockstate) {
    override val name: String
        get() = "Lit Redstone Lamp"

    override val lightLevel: Int
        get() = 15

    override fun toItem(): Item {
        return ItemBlock(BlockRedstoneLamp.properties.defaultState, "")
    }

    override fun onUpdate(type: Int): Int {
        if (!Server.instance.settings.levelSettings.enableRedstone) {
            return 0
        }

        if ((type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE) && !this.isGettingPower) {
            level.scheduleUpdate(this, 4)
            return 1
        }

        if (type == Level.BLOCK_UPDATE_SCHEDULED && !this.isGettingPower) {
            // Redstone event
            val ev = RedstoneUpdateEvent(this)
            Server.instance.pluginManager.callEvent(ev)
            if (ev.cancelled) {
                return 0
            }

            level.updateComparatorOutputLevelSelective(this.position, true)

            level.setBlock(this.position, get(BlockID.REDSTONE_LAMP), false, false)
        }
        return 0
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.LIT_REDSTONE_LAMP)
    }
}