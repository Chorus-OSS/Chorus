package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.event.block.BlockFadeEvent
import org.chorus_oss.chorus.level.Level
import org.chorus_oss.chorus.math.BlockFace
import java.util.concurrent.ThreadLocalRandom

open class BlockBubbleCoral @JvmOverloads constructor(blockstate: BlockState = properties.defaultState) :
    BlockCoral(blockstate) {
    override fun isDead(): Boolean {
        return false
    }

    override fun getDeadCoral(): Block {
        return BlockDeadBubbleCoral()
    }

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (this.id == BlockID.BUBBLE_CORAL) {
                level.scheduleUpdate(this, 60 + ThreadLocalRandom.current().nextInt(40))
            }
            return type
        } else if (type == Level.BLOCK_UPDATE_SCHEDULED) {
            for (face in BlockFace.entries) {
                if (getSideAtLayer(0, face) is BlockFlowingWater ||
                    getSideAtLayer(1, face) is BlockFlowingWater ||
                    getSideAtLayer(0, face) is BlockFrostedIce || getSideAtLayer(1, face) is BlockFrostedIce
                ) {
                    return type
                }
            }
            val event = BlockFadeEvent(this, getDeadCoral())
            if (!event.cancelled) {
                level.setBlock(this.position, event.newState, direct = true, update = true)
            }
            return type
        }
        return 0
    }

    override val properties: BlockProperties
        get() = Companion.properties

    companion object {
        val properties: BlockProperties = BlockProperties(BlockID.BUBBLE_CORAL)
    }
}