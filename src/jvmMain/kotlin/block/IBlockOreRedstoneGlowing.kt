package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.Server
import org.chorus_oss.chorus.event.block.BlockFadeEvent
import org.chorus_oss.chorus.item.Item
import org.chorus_oss.chorus.level.Level

interface IBlockOreRedstoneGlowing {
    val unlitBlock: Block

    val litBlock: Block?

    val level: Level

    fun toItem(): Item {
        return unlitBlock.toItem()
    }

    fun onUpdate(block: Block, type: Int): Int {
        if (type == Level.BLOCK_UPDATE_SCHEDULED || type == Level.BLOCK_UPDATE_RANDOM) {
            val level = level
            val event = BlockFadeEvent(block, unlitBlock)
            Server.instance.pluginManager.callEvent(event)
            if (!event.cancelled) {
                level.setBlock(block.position, event.newState, direct = true, update = true)
            }

            return Level.BLOCK_UPDATE_WEAK
        }
        return 0
    }
}
