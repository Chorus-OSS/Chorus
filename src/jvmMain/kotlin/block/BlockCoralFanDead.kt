package org.chorus_oss.chorus.block

import org.chorus_oss.chorus.level.Level

abstract class BlockCoralFanDead(blockstate: BlockState) : BlockCoralFan(blockstate) {
    override val name: String
        get() = "Dead " + super.name

    override val isDead: Boolean
        get() = true

    override fun onUpdate(type: Int): Int {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!getSide(rootsFace).isSolid) {
                level.useBreakOn(this.position)
            }
            return type
        } else if (type == Level.BLOCK_UPDATE_RANDOM) {
            return super.onUpdate(type)
        }
        return 0
    }

    override fun getDeadCoralFan(): Block {
        return this
    }
}
